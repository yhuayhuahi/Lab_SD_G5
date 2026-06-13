import { Request, Response } from 'express';
import { AxiosError } from 'axios';
import { Pedido, generarPedidoId } from '../models/Pedido.js';
import * as inventario from '../services/inventarioClient.js';
import * as facturacion from '../services/facturacionClient.js';
import * as transporte from '../services/transporteClient.js';
import { enviarNotificacion } from '../services/notificacionesClient.js';
import logger from '../utils/logger.js';

interface ItemRequest {
  productoId: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

interface CreatePedidoBody {
  clienteId: string;
  clienteEmail: string;
  clienteNombre: string;
  clienteDireccion?: string;
  clienteRuc?: string;
  items: ItemRequest[];
  promocionId?: string;
  notas?: string;
}

class InsufficientStockError extends Error {
  detalles: unknown[];
  constructor(detalles: unknown[]) {
    super('INSUFFICIENT_STOCK');
    this.detalles = detalles;
  }
}

function isAxiosError(err: unknown): err is AxiosError {
  return (err as AxiosError).isAxiosError === true;
}

function esConexionFallida(err: unknown): boolean {
  if (!isAxiosError(err)) return false;
  return ['ECONNREFUSED', 'ETIMEDOUT', 'ECONNABORTED', 'ENOTFOUND'].includes(err.code ?? '');
}

function validarBody(body: Partial<CreatePedidoBody>): string | null {
  if (!body.clienteId) return 'El campo clienteId es requerido';
  if (!body.clienteEmail) return 'El campo clienteEmail es requerido';
  if (!body.clienteNombre) return 'El campo clienteNombre es requerido';
  if (!body.items || !Array.isArray(body.items) || body.items.length === 0)
    return 'El campo items es requerido y debe ser un array no vacío';
  return null;
}

async function procesarCreacionPedido(body: CreatePedidoBody) {
  const { clienteId, clienteEmail, clienteNombre, clienteDireccion, clienteRuc, items, promocionId, notas } = body;

  const pedidoId = await generarPedidoId();
  const inicio = new Date();

  // Paso 1: Validar stock en inventario
  const stockItems = items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad }));
  let stockResult;
  try {
    stockResult = await inventario.validarStock(stockItems);
  } catch (err) {
    throw Object.assign(new Error('El servicio de inventario no responde. Intente nuevamente'), {
      tipo: 'SERVICE_UNAVAILABLE',
    });
  }

  if (!stockResult.disponible) {
    const faltantes = stockResult.detalles
      .filter((d) => !d.disponible)
      .map((d) => ({
        productoId: d.productoId,
        nombre: items.find((i) => i.productoId === d.productoId)?.nombre ?? d.productoId,
        solicitado: d.solicitado,
        stockActual: d.stockActual,
        faltante: d.faltante,
      }));
    throw new InsufficientStockError(faltantes);
  }

  // Paso 2: Reservar stock
  try {
    await inventario.reservarStock(pedidoId, stockItems);
  } catch (err) {
    throw Object.assign(new Error('No se pudo reservar el stock. Intente nuevamente'), {
      tipo: 'SERVICE_UNAVAILABLE',
    });
  }

  // Paso 3: Calcular totales con promoción
  const factItems = items.map((i) => ({
    productoId: i.productoId,
    cantidad: i.cantidad,
    precioUnitario: i.precioUnitario,
  }));
  let calculo;
  try {
    calculo = await facturacion.calcularFactura(factItems, promocionId);
  } catch (err) {
    await inventario.liberarStock(pedidoId, stockItems).catch(() => {});
    throw Object.assign(new Error('El servicio de facturación no responde. Intente nuevamente'), {
      tipo: 'SERVICE_UNAVAILABLE',
    });
  }

  // Paso 4: Generar factura
  const itemsFactura = items.map((i) => ({
    productoId: i.productoId,
    nombre: i.nombre,
    cantidad: i.cantidad,
    precioUnitario: i.precioUnitario,
    descuentoAplicado: 0,
  }));
  let factura;
  try {
    factura = await facturacion.generarFactura({
      pedidoId,
      clienteId,
      clienteNombre,
      clienteRuc,
      clienteDireccion,
      items: itemsFactura,
      subtotal: calculo.subtotal,
      descuentoTotal: calculo.descuento,
      total: calculo.total,
      promocionAplicada: calculo.promocionAplicada,
    });
  } catch (err) {
    await inventario.liberarStock(pedidoId, stockItems).catch(() => {});
    if (isAxiosError(err) && err.response?.data) {
      const data = err.response.data as Record<string, unknown>;
      if (data['error'] === 'DUPLICATE_INVOICE') {
        throw Object.assign(new Error('Ya existe una factura para este pedido'), {
          tipo: 'DUPLICATE_INVOICE',
          facturaId: data['facturaExistente'],
        });
      }
    }
    throw Object.assign(new Error('No se pudo generar la factura. Intente nuevamente'), {
      tipo: 'SERVICE_UNAVAILABLE',
    });
  }

  // Paso 5: Asignar transporte (no crítico: si falla, el pedido queda confirmado sin transportista)
  let transporteResult: Awaited<ReturnType<typeof transporte.asignarTransporte>> | null = null;
  try {
    transporteResult = await transporte.asignarTransporte(pedidoId, clienteDireccion ?? '', 'ALTA');
  } catch (err) {
    logger.warn(`No se pudo asignar transporte para ${pedidoId}, el pedido se registrará sin transportista`);
  }

  // Paso 6: Notificar al cliente (fire-and-forget, no bloquea)
  enviarNotificacion({
    email: clienteEmail,
    asunto: 'Confirmación de Pedido - LogiFresh',
    mensaje: `Estimado cliente, su pedido ${pedidoId} ha sido confirmado. Total: S/${calculo.total.toFixed(2)}. Tiempo estimado de entrega: ${transporteResult?.tiempoEstimadoMin ?? 'N/A'} minutos.`,
    pedidoId,
    template: 'PEDIDO_CONFIRMADO',
    datosAdicionales: {
      total: calculo.total,
      transportistaId: transporteResult?.transportistaId,
      tiempoEstimadoMin: transporteResult?.tiempoEstimadoMin,
    },
  });

  // Paso 7: Persistir en MongoDB
  const itemsConSubtotal = items.map((i) => ({
    productoId: i.productoId,
    nombre: i.nombre,
    cantidad: i.cantidad,
    precioUnitario: i.precioUnitario,
    subtotal: parseFloat((i.cantidad * i.precioUnitario).toFixed(2)),
  }));

  const pedido = new Pedido({
    pedidoId,
    clienteId,
    clienteEmail,
    clienteNombre,
    clienteDireccion,
    estado: 'CONFIRMADO',
    items: itemsConSubtotal,
    promocionId,
    notas,
    subtotal: calculo.subtotal,
    descuento: calculo.descuento,
    total: calculo.total,
    facturaId: factura.facturaId,
    transportistaId: transporteResult?.transportistaId,
    tiempoEstimadoEntregaMin: transporteResult?.tiempoEstimadoMin,
    estadoTransporte: transporteResult ? 'ASIGNADO' : undefined,
    historialEstados: [
      { estado: 'PENDIENTE', fecha: inicio },
      { estado: 'CONFIRMADO', fecha: new Date() },
    ],
  });
  await pedido.save();

  return {
    pedidoId,
    clienteId,
    clienteEmail,
    fechaCreacion: pedido.fechaCreacion,
    estado: 'CONFIRMADO',
    items: itemsConSubtotal,
    subtotal: calculo.subtotal,
    descuento: calculo.descuento,
    total: calculo.total,
    facturaId: factura.facturaId,
    transportistaId: transporteResult?.transportistaId,
    tiempoEstimadoEntregaMin: transporteResult?.tiempoEstimadoMin,
  };
}

export async function createPedido(req: Request, res: Response): Promise<void> {
  const body = req.body as Partial<CreatePedidoBody>;

  const errorValidacion = validarBody(body);
  if (errorValidacion) {
    res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: errorValidacion,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  logger.info(`Iniciando creación de pedido para cliente ${body.clienteId}`);

  try {
    const resultado = await procesarCreacionPedido(body as CreatePedidoBody);
    logger.info(`Pedido ${resultado.pedidoId} creado exitosamente`);
    res.status(201).json(resultado);
  } catch (err: unknown) {
    logger.error('Error al crear pedido', err);

    if (err instanceof InsufficientStockError) {
      res.status(409).json({
        error: 'INSUFFICIENT_STOCK',
        mensaje: 'No hay suficiente stock para completar el pedido',
        detalles: err.detalles,
        timestamp: new Date().toISOString(),
      });
      return;
    }

    const errObj = err as Record<string, unknown>;

    if (errObj['tipo'] === 'DUPLICATE_INVOICE') {
      res.status(409).json({
        error: 'DUPLICATE_INVOICE',
        mensaje: errObj['message'],
        facturaId: errObj['facturaId'],
        timestamp: new Date().toISOString(),
      });
      return;
    }

    if (errObj['tipo'] === 'SERVICE_UNAVAILABLE' || esConexionFallida(err)) {
      res.status(500).json({
        error: 'SERVICE_UNAVAILABLE',
        mensaje: (err as Error).message || 'Un servicio dependiente no responde. Intente nuevamente',
        timestamp: new Date().toISOString(),
      });
      return;
    }

    res.status(500).json({
      error: 'SERVICE_UNAVAILABLE',
      mensaje: 'Error interno del servidor',
      timestamp: new Date().toISOString(),
    });
  }
}

export async function getPedido(req: Request, res: Response): Promise<void> {
  try {
    const { id } = req.params;
    const pedido = await Pedido.findOne({ pedidoId: id });

    if (!pedido) {
      res.status(404).json({
        error: 'NOT_FOUND',
        mensaje: `No existe un pedido con ID: ${id}`,
        timestamp: new Date().toISOString(),
      });
      return;
    }

    res.status(200).json({
      pedidoId: pedido.pedidoId,
      clienteId: pedido.clienteId,
      clienteEmail: pedido.clienteEmail,
      clienteNombre: pedido.clienteNombre,
      fechaCreacion: pedido.fechaCreacion,
      fechaActualizacion: pedido.fechaActualizacion,
      estado: pedido.estado,
      items: pedido.items,
      total: pedido.total,
      facturaId: pedido.facturaId,
      transportistaId: pedido.transportistaId,
      estadoTransporte: pedido.estadoTransporte,
      historialEstados: pedido.historialEstados,
    });
  } catch (err) {
    logger.error('Error al obtener pedido', err);
    res.status(500).json({
      error: 'SERVICE_UNAVAILABLE',
      mensaje: 'Error interno del servidor',
      timestamp: new Date().toISOString(),
    });
  }
}

export async function cancelarPedido(req: Request, res: Response): Promise<void> {
  try {
    const { id } = req.params;
    const { motivo } = (req.body ?? {}) as { motivo?: string };

    const pedido = await Pedido.findOne({ pedidoId: id });

    if (!pedido) {
      res.status(404).json({
        error: 'NOT_FOUND',
        mensaje: `No existe un pedido con ID: ${id}`,
        timestamp: new Date().toISOString(),
      });
      return;
    }

    if (pedido.estado === 'CANCELADO') {
      res.status(400).json({
        error: 'INVALID_STATE',
        mensaje: `El pedido ${id} ya se encuentra CANCELADO`,
        estadoActual: 'CANCELADO',
        timestamp: new Date().toISOString(),
      });
      return;
    }

    if (pedido.estado === 'ENTREGADO') {
      res.status(400).json({
        error: 'INVALID_STATE',
        mensaje: 'No se puede cancelar un pedido ya ENTREGADO',
        estadoActual: 'ENTREGADO',
        timestamp: new Date().toISOString(),
      });
      return;
    }

    const estadoAnterior = pedido.estado;
    const ahora = new Date();

    // Liberar stock reservado
    const stockItems = pedido.items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad }));
    let stockLiberado = false;
    try {
      await inventario.liberarStock(pedido.pedidoId, stockItems);
      stockLiberado = true;
    } catch (err) {
      logger.warn(`No se pudo liberar stock para pedido ${id}`);
    }

    // Actualizar estado en MongoDB
    pedido.estado = 'CANCELADO';
    pedido.fechaActualizacion = ahora;
    pedido.historialEstados.push({ estado: 'CANCELADO', fecha: ahora });
    await pedido.save();

    logger.info(`Pedido ${id} cancelado. Motivo: ${motivo ?? 'No especificado'}`);

    res.status(200).json({
      pedidoId: id,
      estadoAnterior,
      estadoActual: 'CANCELADO',
      fechaCancelacion: ahora.toISOString(),
      motivo: motivo ?? null,
      recursosLiberados: {
        stock: stockLiberado,
        factura: false,
        transporte: false,
      },
    });
  } catch (err) {
    logger.error('Error al cancelar pedido', err);
    res.status(500).json({
      error: 'SERVICE_UNAVAILABLE',
      mensaje: 'Error interno del servidor',
      timestamp: new Date().toISOString(),
    });
  }
}

export async function createPedidoLento(req: Request, res: Response): Promise<void> {
  logger.warn('Endpoint lento activado: esperando 9 segundos antes de procesar...');
  await new Promise((resolve) => setTimeout(resolve, 9000));
  await createPedido(req, res);
}

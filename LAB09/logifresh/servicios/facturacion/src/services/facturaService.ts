import { randomBytes } from "crypto";
import { pool } from "../db";
import { PromocionService } from "./promocionService";

const promocionService = new PromocionService();

interface ItemInput {
  productoId: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

interface GenerarFacturaDTO {
  pedidoId: string;
  clienteId: string;
  clienteNombre?: string;
  clienteRuc?: string;
  clienteDireccion?: string;
  items: ItemInput[];
  promocionId?: string | null;
}

function generarFacturaId(): string {
  const sufijo = randomBytes(3).toString("hex").toUpperCase();
  return `FAC-${Date.now()}-${sufijo}`;
}

export class FacturaService {
  async calcular(items: ItemInput[], promocionId: string | null) {
    return promocionService.calcular(items, promocionId);
  }

  async generarFactura(data: GenerarFacturaDTO) {
    const client = await pool.connect();

    try {
      await client.query("BEGIN");

      // 1. Verificar si ya existe factura para este pedido
      const facturaExistente = await client.query(
        "SELECT factura_id FROM facturas WHERE pedido_id = $1",
        [data.pedidoId]
      );

      if (facturaExistente.rows.length > 0) {
        throw new Error(`DUPLICATE_INVOICE:${facturaExistente.rows[0].factura_id}`);
      }

      // 2. Calcular totales con promociones
      const calculo = promocionService.calcular(data.items, data.promocionId || null);
      const igv = parseFloat((calculo.total * 0.18).toFixed(2));
      const totalConIgv = parseFloat((calculo.total + igv).toFixed(2));

      // 3. Insertar factura
      const facturaId = generarFacturaId();
      const facturaResult = await client.query(
        `INSERT INTO facturas
         (factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion,
          subtotal, descuento, total, igv, estado, promocion_aplicada)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
         RETURNING id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc,
                   cliente_direccion, subtotal, descuento, total, igv, fecha_emision,
                   estado, promocion_aplicada`,
        [
          facturaId,
          data.pedidoId,
          data.clienteId,
          data.clienteNombre || null,
          data.clienteRuc || null,
          data.clienteDireccion || null,
          calculo.subtotal,
          calculo.descuento,
          calculo.total,
          igv,
          "EMITIDA",
          calculo.promocionAplicada,
        ]
      );

      const factura = facturaResult.rows[0];

      // 4. Insertar items en batch (1 sola query en lugar de N queries secuenciales)
      if (data.items.length > 0) {
        const descMap = new Map(
          calculo.itemsConDescuento.map((i) => [i.productoId, i.descuentoAplicado])
        );
        const placeholders = data.items
          .map((_, i) => `($${i * 6 + 1}, $${i * 6 + 2}, $${i * 6 + 3}, $${i * 6 + 4}, $${i * 6 + 5}, $${i * 6 + 6})`)
          .join(", ");
        const valores = data.items.flatMap((item) => [
          factura.id,
          item.productoId,
          item.nombre,
          item.cantidad,
          item.precioUnitario,
          descMap.get(item.productoId) ?? 0,
        ]);
        await client.query(
          `INSERT INTO items_factura
           (factura_id, producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado)
           VALUES ${placeholders}`,
          valores
        );
      }

      await client.query("COMMIT");

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } catch (error: any) {
      await client.query("ROLLBACK");
      // Capturar violación de UNIQUE en pedido_id a nivel DB (race condition)
      if (error.code === "23505") {
        const existing = await pool.query(
          "SELECT factura_id FROM facturas WHERE pedido_id = $1",
          [data.pedidoId]
        );
        const existingId = existing.rows[0]?.factura_id ?? "unknown";
        throw new Error(`DUPLICATE_INVOICE:${existingId}`);
      }
      throw error;
    } finally {
      client.release();
    }
  }

  async obtenerFacturaPorId(facturaId: string) {
    const client = await pool.connect();
    try {
      const facturaResult = await client.query(
        `SELECT id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion,
                subtotal, descuento, total, igv, fecha_emision, estado, promocion_aplicada
         FROM facturas WHERE factura_id = $1`,
        [facturaId]
      );

      if (facturaResult.rows.length === 0) return null;

      const factura = facturaResult.rows[0];

      const itemsResult = await client.query(
        `SELECT producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado
         FROM items_factura WHERE factura_id = $1`,
        [factura.id]
      );

      const totalConIgv = Number(factura.total) + Number(factura.igv);

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        items: itemsResult.rows.map((item: any) => ({
          productoId: item.producto_id,
          nombre: item.nombre_producto,
          cantidad: item.cantidad,
          precioUnitario: Number(item.precio_unitario),
          descuentoAplicado: Number(item.descuento_aplicado),
          subtotal: Number(item.precio_unitario) * item.cantidad - Number(item.descuento_aplicado),
        })),
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } finally {
      client.release();
    }
  }

  async obtenerFacturaPorPedido(pedidoId: string) {
    const client = await pool.connect();
    try {
      const facturaResult = await client.query(
        `SELECT id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion,
                subtotal, descuento, total, igv, fecha_emision, estado, promocion_aplicada
         FROM facturas WHERE pedido_id = $1`,
        [pedidoId]
      );

      if (facturaResult.rows.length === 0) return null;

      const factura = facturaResult.rows[0];

      const itemsResult = await client.query(
        `SELECT producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado
         FROM items_factura WHERE factura_id = $1`,
        [factura.id]
      );

      const totalConIgv = Number(factura.total) + Number(factura.igv);

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        items: itemsResult.rows.map((item: any) => ({
          productoId: item.producto_id,
          nombre: item.nombre_producto,
          cantidad: item.cantidad,
          precioUnitario: Number(item.precio_unitario),
          descuentoAplicado: Number(item.descuento_aplicado),
          subtotal: Number(item.precio_unitario) * item.cantidad - Number(item.descuento_aplicado),
        })),
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } finally {
      client.release();
    }
  }
}

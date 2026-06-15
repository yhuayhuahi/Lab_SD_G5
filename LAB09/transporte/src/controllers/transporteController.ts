import type { Request, Response } from 'express';
import {
  asignarCamion,
  actualizarEstado,
  obtenerEstado,
  listarCamiones,
  TransporteError
} from '../services/transporteService';

function obtenerTiempoEstimado(): number {
  const valor = Number(process.env.TIEMPO_ESTIMADO_MIN ?? '45');
  return Number.isFinite(valor) && valor > 0 ? valor : 45;
}

function obtenerTimestamp(): string {
  return new Date().toISOString();
}

function responderError(res: Response, error: unknown): Response {
  if (error instanceof TransporteError) {
    if (error.code === 'NO_TRUCKS_AVAILABLE') {
      return res.status(400).json({
        error: error.code,
        mensaje: error.message,
        tiempoEstimadoEsperaMin: error.details?.tiempoEstimadoEsperaMin ?? 15,
        timestamp: obtenerTimestamp()
      });
    }

    if (error.code === 'NOT_FOUND') {
      return res.status(404).json({
        error: error.code,
        mensaje: error.message,
        timestamp: obtenerTimestamp()
      });
    }

    if (error.code === 'INVALID_STATE_TRANSITION') {
      return res.status(400).json({
        error: error.code,
        mensaje: error.message,
        estadoActual: error.details?.estadoActual,
        estadosPermitidos: error.details?.estadosPermitidos,
        timestamp: obtenerTimestamp()
      });
    }

    return res.status(400).json({
      error: error.code,
      mensaje: error.message,
      timestamp: obtenerTimestamp()
    });
  }

  const mensaje = error instanceof Error ? error.message : 'Error inesperado';
  return res.status(500).json({
    error: 'SERVICE_UNAVAILABLE',
    mensaje,
    timestamp: obtenerTimestamp()
  });
}

export function asignar(req: Request, res: Response): Response {
  const pedidoId = typeof req.body?.pedidoId === 'string' ? req.body.pedidoId.trim() : '';

  if (!pedidoId) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo pedidoId es requerido',
      timestamp: obtenerTimestamp()
    });
  }

  try {
    const transporte = asignarCamion(pedidoId, obtenerTiempoEstimado());
    return res.status(200).json(transporte);
  } catch (error) {
    return responderError(res, error);
  }
}

export function consultarEstado(req: Request, res: Response): Response {
  const pedidoId = typeof req.params.pedidoId === 'string' ? req.params.pedidoId.trim() : '';

  if (!pedidoId) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo pedidoId es requerido',
      timestamp: obtenerTimestamp()
    });
  }

  const transporte = obtenerEstado(pedidoId);

  if (!transporte) {
    return res.status(404).json({
      error: 'NOT_FOUND',
      mensaje: `No hay registro de transporte para el pedido: ${pedidoId}`,
      timestamp: obtenerTimestamp()
    });
  }

  return res.json(transporte);
}

export function cambiarEstado(req: Request, res: Response): Response {
  const pedidoId = typeof req.params.pedidoId === 'string' ? req.params.pedidoId.trim() : '';
  const estado = typeof req.body?.estado === 'string' ? req.body.estado.trim() : '';

  if (!pedidoId) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo pedidoId es requerido',
      timestamp: obtenerTimestamp()
    });
  }

  if (!estado) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo estado es requerido',
      timestamp: obtenerTimestamp()
    });
  }

  try {
    const transporte = actualizarEstado(pedidoId, estado);
    return res.json(transporte);
  } catch (error) {
    return responderError(res, error);
  }
}

export function camionesDisponibles(req: Request, res: Response): Response {
  const estado = typeof req.query.estado === 'string' ? req.query.estado.trim().toUpperCase() : undefined;
  const tipo = typeof req.query.tipo === 'string' ? req.query.tipo.trim().toUpperCase() : undefined;
  const camiones = listarCamiones({
    estado: estado === 'DISPONIBLE' || estado === 'OCUPADO' || estado === 'MANTENIMIENTO' ? estado : undefined,
    tipo: tipo === 'REFRIGERADO' || tipo === 'SECO' ? tipo : undefined
  });

  return res.json({
    total: camiones.length,
    disponibles: camiones.filter((camion) => camion.estado === 'DISPONIBLE').length,
    camiones,
    timestamp: obtenerTimestamp()
  });
}
const {
  validarStock,
  reservarStock,
  liberarStock,
  obtenerStock,
  obtenerStockReservado,
  actualizarStock,
} = require('../services/inventarioService');

const ts = () => new Date().toISOString();

// ─── Validación de items ──────────────────────────────────────────────────────

function validarItems(items) {
  if (!items || !Array.isArray(items) || items.length === 0) {
    return 'El campo items es requerido y debe ser un array no vacío';
  }
  for (const item of items) {
    if (!item.productoId || typeof item.productoId !== 'string') {
      return 'Cada item debe tener un productoId válido (string)';
    }
    if (item.cantidad == null || !Number.isInteger(item.cantidad) || item.cantidad <= 0) {
      return `El campo cantidad de "${item.productoId}" debe ser un entero mayor a 0`;
    }
  }
  return null;
}

// ─── Handlers ─────────────────────────────────────────────────────────────────

const validar = async (req, res) => {
  try {
    const { items } = req.body;

    const errorItems = validarItems(items);
    if (errorItems) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: errorItems,
        timestamp: ts(),
      });
    }

    const resultado = await validarStock(items);

    return res.status(200).json({
      disponible: resultado.disponible,
      detalles: resultado.detalles,
      timestamp: ts(),
    });
  } catch (err) {
    console.error('[Inventario] Error en /validar:', err.message);
    return res.status(503).json({
      error: 'SERVICE_ERROR',
      mensaje: 'Error al consultar inventario. Intente nuevamente.',
      timestamp: ts(),
    });
  }
};

const reservar = async (req, res) => {
  try {
    const { pedidoId, items } = req.body;

    if (!pedidoId || typeof pedidoId !== 'string' || pedidoId.trim() === '') {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'El campo pedidoId es requerido',
        timestamp: ts(),
      });
    }

    const errorItems = validarItems(items);
    if (errorItems) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: errorItems,
        timestamp: ts(),
      });
    }

    const resultado = await reservarStock(pedidoId.trim(), items);

    if (!resultado.success) {
      return res.status(409).json({
        error: 'RESERVATION_FAILED',
        mensaje: 'Stock insuficiente para uno o más productos',
        detalles: resultado.faltantes,
        timestamp: ts(),
      });
    }

    return res.status(200).json({
      success: true,
      mensaje: 'Stock reservado exitosamente',
      reservas: resultado.reservas,
      timestamp: ts(),
    });
  } catch (err) {
    console.error('[Inventario] Error en /reservar:', err.message);
    return res.status(503).json({
      error: 'SERVICE_ERROR',
      mensaje: 'Error al reservar stock. Intente nuevamente.',
      timestamp: ts(),
    });
  }
};

const liberar = async (req, res) => {
  try {
    const { pedidoId, items } = req.body;

    if (!pedidoId || typeof pedidoId !== 'string' || pedidoId.trim() === '') {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'El campo pedidoId es requerido',
        timestamp: ts(),
      });
    }

    const errorItems = validarItems(items);
    if (errorItems) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: errorItems,
        timestamp: ts(),
      });
    }

    const liberaciones = await liberarStock(pedidoId.trim(), items);

    return res.status(200).json({
      success: true,
      mensaje: 'Stock liberado exitosamente',
      liberaciones,
      timestamp: ts(),
    });
  } catch (err) {
    console.error('[Inventario] Error en /liberar:', err.message);
    return res.status(503).json({
      error: 'SERVICE_ERROR',
      mensaje: 'Error al liberar stock. Intente nuevamente.',
      timestamp: ts(),
    });
  }
};

const consultarStock = async (req, res) => {
  try {
    const { productoId } = req.params;

    if (!productoId || typeof productoId !== 'string') {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'productoId inválido',
        timestamp: ts(),
      });
    }

    const stockActual = await obtenerStock(productoId);

    if (stockActual === null) {
      return res.status(404).json({
        error: 'NOT_FOUND',
        mensaje: `No existe producto con ID: ${productoId}`,
        timestamp: ts(),
      });
    }

    return res.status(200).json({
      productoId,
      stockActual,
      stockReservado: 0,
      stockDisponible: stockActual,
      ultimaActualizacion: ts(),
    });
  } catch (err) {
    console.error('[Inventario] Error en /stock/:productoId:', err.message);
    return res.status(503).json({
      error: 'SERVICE_ERROR',
      mensaje: 'Error al consultar stock.',
      timestamp: ts(),
    });
  }
};

const actualizarStockHandler = async (req, res) => {
  try {
    const { productoId } = req.params;
    const { operacion, cantidad, motivo } = req.body;

    const operacionesValidas = ['INCREMENTAR', 'DECREMENTAR', 'SET'];
    if (!operacion || !operacionesValidas.includes(operacion)) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: `operacion inválida. Valores permitidos: ${operacionesValidas.join(', ')}`,
        timestamp: ts(),
      });
    }

    // SET acepta cantidad >= 0 (resetear a 0 es válido), los demás requieren > 0
    if (cantidad == null || !Number.isFinite(Number(cantidad))) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'El campo cantidad es requerido y debe ser numérico',
        timestamp: ts(),
      });
    }
    const cantidadNum = Number(cantidad);
    if (operacion === 'SET' && cantidadNum < 0) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'Para SET la cantidad debe ser >= 0',
        timestamp: ts(),
      });
    }
    if (operacion !== 'SET' && cantidadNum <= 0) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'La cantidad debe ser > 0',
        timestamp: ts(),
      });
    }

    const resultado = await actualizarStock(productoId, operacion, cantidadNum);

    if (resultado === null) {
      return res.status(404).json({
        error: 'NOT_FOUND',
        mensaje: `No existe producto con ID: ${productoId}`,
        timestamp: ts(),
      });
    }

    return res.status(200).json({
      productoId,
      operacion,
      motivo: motivo || null,
      valorAnterior: resultado.valorAnterior,
      valorNuevo: resultado.valorNuevo,
      timestamp: ts(),
    });
  } catch (err) {
    console.error('[Inventario] Error en PUT /stock/:productoId:', err.message);
    return res.status(503).json({
      error: 'SERVICE_ERROR',
      mensaje: 'Error al actualizar stock.',
      timestamp: ts(),
    });
  }
};

module.exports = {
  validar,
  reservar,
  liberar,
  consultarStock,
  actualizarStockHandler,
};

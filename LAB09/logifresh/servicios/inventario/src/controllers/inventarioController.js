const {
  validarStock,
  reservarStock,
  liberarStock,
  obtenerStock,
  obtenerStockReservado,
  actualizarStock,
} = require('../services/inventarioService');

const ts = () => new Date().toISOString();

const validar = async (req, res) => {
  const { items } = req.body;

  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo items es requerido y debe ser un array no vacío',
      timestamp: ts(),
    });
  }

  for (const item of items) {
    if (!item.productoId || item.cantidad == null || item.cantidad <= 0) {
      return res.status(400).json({
        error: 'VALIDATION_ERROR',
        mensaje: 'Cada item debe tener productoId y cantidad mayor a 0',
        timestamp: ts(),
      });
    }
  }

  const resultado = await validarStock(items);

  return res.status(200).json({
    disponible: resultado.disponible,
    detalles: resultado.detalles,
    timestamp: ts(),
  });
};

const reservar = async (req, res) => {
  const { pedidoId, items } = req.body;

  if (!pedidoId) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo pedidoId es requerido',
      timestamp: ts(),
    });
  }
  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo items es requerido y debe ser un array no vacío',
      timestamp: ts(),
    });
  }

  const resultado = await reservarStock(pedidoId, items);

  if (!resultado.success) {
    return res.status(409).json({
      error: 'RESERVATION_FAILED',
      mensaje: 'No se pudo reservar el stock. Stock insuficiente',
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
};

const liberar = async (req, res) => {
  const { pedidoId, items } = req.body;

  if (!pedidoId) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo pedidoId es requerido',
      timestamp: ts(),
    });
  }
  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo items es requerido y debe ser un array no vacío',
      timestamp: ts(),
    });
  }

  const liberaciones = await liberarStock(pedidoId, items);

  return res.status(200).json({
    success: true,
    mensaje: 'Stock liberado exitosamente',
    liberaciones,
    timestamp: ts(),
  });
};

const consultarStock = async (req, res) => {
  const { productoId } = req.params;

  const stockActual = await obtenerStock(productoId);

  if (stockActual === null) {
    return res.status(404).json({
      error: 'NOT_FOUND',
      mensaje: `No existe producto con ID: ${productoId}`,
      timestamp: ts(),
    });
  }

  const stockReservado = await obtenerStockReservado(productoId);
  const stockDisponible = stockActual - stockReservado;

  return res.status(200).json({
    productoId,
    stockActual,
    stockReservado,
    stockDisponible,
    ultimaActualizacion: ts(),
  });
};

const actualizarStockHandler = async (req, res) => {
  const { productoId } = req.params;
  const { operacion, cantidad, motivo } = req.body;

  const operacionesValidas = ['INCREMENTAR', 'DECREMENTAR', 'SET'];
  if (!operacion || !operacionesValidas.includes(operacion)) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: `El campo operacion es requerido. Valores válidos: ${operacionesValidas.join(', ')}`,
      timestamp: ts(),
    });
  }
  if (cantidad == null || cantidad <= 0) {
    return res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: 'El campo cantidad es requerido y debe ser mayor a 0',
      timestamp: ts(),
    });
  }

  const resultado = await actualizarStock(productoId, operacion, cantidad);

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
};

module.exports = {
  validar,
  reservar,
  liberar,
  consultarStock,
  actualizarStockHandler,
};

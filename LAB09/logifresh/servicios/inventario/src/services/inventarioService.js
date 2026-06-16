const { client } = require('../config/redis');

const PRODUCTOS_INICIALES = {
  'PROD-YOGURT': 150,
  'PROD-LECHE': 45,
  'PROD-QUELLAVES': 0,
  'PROD-MANTECA': 23,
  'PROD-POLLO': 78,
};

async function inicializarStock() {
  for (const [productoId, cantidad] of Object.entries(PRODUCTOS_INICIALES)) {
    const existe = await client.exists(`stock:${productoId}`);
    if (!existe) {
      await client.set(`stock:${productoId}`, cantidad);
    }
  }
  console.log('[Inventario] Stock inicial cargado en Redis');
}

async function obtenerStock(productoId) {
  const valor = await client.get(`stock:${productoId}`);
  if (valor === null) return null;
  return parseInt(valor, 10);
}

async function obtenerStockReservado(productoId) {
  const keys = await client.keys(`reserva:*:${productoId}`);
  let total = 0;
  for (const key of keys) {
    const v = await client.get(key);
    total += parseInt(v || '0', 10);
  }
  return total;
}

async function validarStock(items) {
  const detalles = [];
  let disponible = true;

  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);
    const stockReservado = await obtenerStockReservado(item.productoId);
    const stockDisponible = stockActual !== null ? stockActual - stockReservado : 0;

    if (stockActual === null || stockDisponible < item.cantidad) {
      disponible = false;
      detalles.push({
        productoId: item.productoId,
        disponible: false,
        stockActual: stockActual ?? 0,
        solicitado: item.cantidad,
        faltante: item.cantidad - Math.max(0, stockDisponible), // Mantener
      });
    } else {
      detalles.push({
        productoId: item.productoId,
        disponible: true,
        stockActual: stockActual,
        solicitado: item.cantidad,
        faltante: 0, // AGREGAR faltante: 0
      });
    }
  }

  return { disponible, detalles };
}

async function reservarStock(pedidoId, items) {
  const faltantes = [];
  const reservas = [];

  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);
    const stockReservado = await obtenerStockReservado(item.productoId);
    const stockDisponible = (stockActual ?? 0) - stockReservado;

    if (stockDisponible < item.cantidad) {
      faltantes.push({
        productoId: item.productoId,
        disponible: stockDisponible,
        solicitado: item.cantidad,
        faltante: item.cantidad - stockDisponible,
      });
    }
  }

  if (faltantes.length > 0) {
    return { success: false, faltantes };
  }

  for (const item of items) {
    await client.set(`reserva:${pedidoId}:${item.productoId}`, item.cantidad);
    const stockActual = await obtenerStock(item.productoId);
    const stockReservado = await obtenerStockReservado(item.productoId);
    reservas.push({
      productoId: item.productoId,
      cantidadReservada: item.cantidad,
      stockRestante: (stockActual ?? 0) - stockReservado,
    });
  }

  return { success: true, reservas };
}

async function liberarStock(pedidoId, items) {
  const liberaciones = [];

  for (const item of items) {
    await client.del(`reserva:${pedidoId}:${item.productoId}`);
    const stockActual = await obtenerStock(item.productoId);
    liberaciones.push({
      productoId: item.productoId,
      cantidadLiberada: item.cantidad,
      stockActual: stockActual ?? 0,
    });
  }

  return liberaciones;
}

async function actualizarStock(productoId, operacion, cantidad) {
  const stockActual = await obtenerStock(productoId);
  if (stockActual === null) return null;

  let nuevoValor;
  if (operacion === 'INCREMENTAR') {
    nuevoValor = stockActual + cantidad;
  } else if (operacion === 'DECREMENTAR') {
    nuevoValor = Math.max(0, stockActual - cantidad);
  } else {
    nuevoValor = cantidad;
  }

  await client.set(`stock:${productoId}`, nuevoValor);
  return { valorAnterior: stockActual, valorNuevo: nuevoValor };
}

module.exports = {
  inicializarStock,
  validarStock,
  reservarStock,
  liberarStock,
  obtenerStock,
  obtenerStockReservado,
  actualizarStock,
};

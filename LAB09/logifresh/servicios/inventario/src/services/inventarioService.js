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
  // Mantenido por compatibilidad con consultarStock; retorna 0 porque
  // las reservas ya se descuentan directamente del stock real.
  return 0;
}

async function validarStock(items) {
  const detalles = [];
  let disponible = true;

  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);

    if (stockActual === null || stockActual < item.cantidad) {
      disponible = false;
      detalles.push({
        productoId: item.productoId,
        disponible: false,
        stockActual: stockActual ?? 0,
        solicitado: item.cantidad,
        faltante: item.cantidad - Math.max(0, stockActual ?? 0),
      });
    } else {
      detalles.push({
        productoId: item.productoId,
        disponible: true,
        stockActual,
        solicitado: item.cantidad,
        faltante: 0,
      });
    }
  }

  return { disponible, detalles };
}

async function reservarStock(pedidoId, items) {
  const faltantes = [];

  // Verificar disponibilidad antes de decrementar
  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);
    const disponible = stockActual ?? 0;

    if (disponible < item.cantidad) {
      faltantes.push({
        productoId: item.productoId,
        disponible,
        solicitado: item.cantidad,
        faltante: item.cantidad - disponible,
      });
    }
  }

  if (faltantes.length > 0) {
    return { success: false, faltantes };
  }

  // Decrementar stock real directamente — no se crean claves de reserva
  // para evitar que se acumulen tras confirmar pedidos
  const reservas = [];
  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);
    const nuevoStock = (stockActual ?? 0) - item.cantidad;
    await client.set(`stock:${item.productoId}`, nuevoStock);
    reservas.push({
      productoId: item.productoId,
      cantidadReservada: item.cantidad,
      stockRestante: nuevoStock,
    });
  }

  return { success: true, reservas };
}

async function liberarStock(pedidoId, items) {
  // Solo se llama en cancelación: devuelve el stock decrementado
  const liberaciones = [];

  for (const item of items) {
    const stockActual = await obtenerStock(item.productoId);
    const nuevoStock = (stockActual ?? 0) + item.cantidad;
    await client.set(`stock:${item.productoId}`, nuevoStock);
    liberaciones.push({
      productoId: item.productoId,
      cantidadLiberada: item.cantidad,
      stockActual: nuevoStock,
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

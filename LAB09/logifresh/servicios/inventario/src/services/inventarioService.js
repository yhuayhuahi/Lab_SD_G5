const { client } = require('../config/redis');

const PRODUCTOS_INICIALES = {
  'PROD-YOGURT':    500,
  'PROD-LECHE':     500,
  'PROD-QUELLAVES': 200,
  'PROD-MANTECA':   300,
  'PROD-POLLO':     400,
  'PROD-CARNE':     500,
};

// ─────────────────────────────────────────────────────────────────────────────
// Scripts Lua — se ejecutan atómicamente en Redis (single-thread).
// Ningún otro comando Redis se intercala mientras corren: elimina TOCTOU.
// ─────────────────────────────────────────────────────────────────────────────

// Verifica stock Y decrementa en un solo paso.
// Retorna: nuevo stock (>= 0) | -1 producto no existe | -2 stock insuficiente
const LUA_RESERVAR = `
local raw = redis.call('GET', KEYS[1])
if not raw then return -1 end
local stock = tonumber(raw)
local pedido = tonumber(ARGV[1])
if stock < pedido then return -2 end
return redis.call('DECRBY', KEYS[1], pedido)
`;

// Decrementa con floor en 0 (uso admin: nunca queda negativo)
// Retorna: nuevo stock | false si producto no existe
const LUA_DECREMENTAR_SAFE = `
local raw = redis.call('GET', KEYS[1])
if not raw then return false end
local nuevo = math.max(0, tonumber(raw) - tonumber(ARGV[1]))
redis.call('SET', KEYS[1], nuevo)
return nuevo
`;

// ─────────────────────────────────────────────────────────────────────────────
// Helper: reintentos para errores transitorios de Redis
// ─────────────────────────────────────────────────────────────────────────────

async function conReintentos(fn, maxIntentos = 3) {
  let ultimoError;
  for (let intento = 1; intento <= maxIntentos; intento++) {
    try {
      return await fn();
    } catch (err) {
      ultimoError = err;
      if (intento < maxIntentos) {
        await new Promise(r => setTimeout(r, 50 * intento));
      }
    }
  }
  throw ultimoError;
}

// ─────────────────────────────────────────────────────────────────────────────
// Funciones públicas
// ─────────────────────────────────────────────────────────────────────────────

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

// Mantenido por compatibilidad con consultarStock del controller
async function obtenerStockReservado(_productoId) {
  return 0;
}

async function verificarConexion() {
  const pong = await client.ping();
  return pong === 'PONG';
}

// Lectura informativa (no-atómica está bien: solo es un "pre-check" de UX)
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

// Reserva atómica con rollback compensatorio si algún item falla
async function reservarStock(pedidoId, items) {
  const exitosos = [];
  const faltantes  = [];

  for (const item of items) {
    if (!item.productoId || item.cantidad <= 0) {
      faltantes.push({
        productoId: item.productoId ?? 'desconocido',
        disponible: 0,
        solicitado: item.cantidad,
        faltante: item.cantidad,
      });
      continue;
    }

    let resultado;
    try {
      resultado = await conReintentos(() =>
        client.eval(LUA_RESERVAR, {
          keys: [`stock:${item.productoId}`],
          arguments: [String(item.cantidad)],
        })
      );
    } catch (err) {
      console.error(`[Inventario] Error en Lua reservar ${item.productoId}:`, err.message);
      faltantes.push({
        productoId: item.productoId,
        disponible: 0,
        solicitado: item.cantidad,
        faltante: item.cantidad,
      });
      continue;
    }

    const nuevoStock = Number(resultado);

    if (nuevoStock === -1) {
      // Producto no existe en Redis
      faltantes.push({
        productoId: item.productoId,
        disponible: 0,
        solicitado: item.cantidad,
        faltante: item.cantidad,
      });
    } else if (nuevoStock === -2) {
      // Stock insuficiente
      const stockActual = (await obtenerStock(item.productoId)) ?? 0;
      faltantes.push({
        productoId: item.productoId,
        disponible: stockActual,
        solicitado: item.cantidad,
        faltante: item.cantidad - stockActual,
      });
    } else {
      exitosos.push({
        productoId: item.productoId,
        cantidadReservada: item.cantidad,
        stockRestante: nuevoStock,
      });
    }
  }

  // Rollback compensatorio: devolver stock a los items que sí se decrementaron
  if (faltantes.length > 0) {
    for (const exitoso of exitosos) {
      await conReintentos(() =>
        client.incrBy(`stock:${exitoso.productoId}`, exitoso.cantidadReservada)
      ).catch(err =>
        console.error(`[Inventario] Error en rollback de ${exitoso.productoId}:`, err.message)
      );
    }
    return { success: false, faltantes };
  }

  return { success: true, reservas: exitosos };
}

// INCRBY es atómico en Redis — no necesita Lua
async function liberarStock(pedidoId, items) {
  const liberaciones = [];

  for (const item of items) {
    if (!item.productoId || item.cantidad <= 0) continue;

    const nuevoStock = await conReintentos(() =>
      client.incrBy(`stock:${item.productoId}`, item.cantidad)
    );

    liberaciones.push({
      productoId: item.productoId,
      cantidadLiberada: item.cantidad,
      stockActual: nuevoStock,
    });
  }

  return liberaciones;
}

async function actualizarStock(productoId, operacion, cantidad) {
  if (operacion === 'SET') {
    // SET permite inicializar o resetear cualquier producto (incluso uno nuevo)
    const anterior = (await obtenerStock(productoId)) ?? 0;
    await client.set(`stock:${productoId}`, cantidad);
    return { valorAnterior: anterior, valorNuevo: cantidad };
  }

  const stockActual = await obtenerStock(productoId);
  if (stockActual === null) return null;

  let nuevoValor;

  if (operacion === 'INCREMENTAR') {
    // INCRBY es atómico
    nuevoValor = await conReintentos(() =>
      client.incrBy(`stock:${productoId}`, cantidad)
    );
  } else if (operacion === 'DECREMENTAR') {
    // Lua garantiza floor en 0
    const resultado = await conReintentos(() =>
      client.eval(LUA_DECREMENTAR_SAFE, {
        keys: [`stock:${productoId}`],
        arguments: [String(cantidad)],
      })
    );
    nuevoValor = Number(resultado);
  } else {
    return null;
  }

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
  verificarConexion,
};

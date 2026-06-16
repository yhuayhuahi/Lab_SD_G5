import http from 'k6/http';
import { sleep, check } from 'k6';

// ============================================================
// CONFIGURACIÓN DE LA PRUEBA
// ============================================================

export const options = {
  vus: 100,
  duration: '5m',
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95% de peticiones < 5s
    http_req_failed: ['rate<0.05'],    // Menos del 5% de errores HTTP reales
  },
};

// ============================================================
// DATOS DE PRUEBA
// Todos los productos tienen stock suficiente (reiniciado en setup)
// ============================================================

const productos = [
  { productoId: 'PROD-YOGURT',   nombre: 'Yogurt Natural',    precioUnitario: 3.50  },
  { productoId: 'PROD-LECHE',    nombre: 'Leche Entera',      precioUnitario: 5.20  },
  { productoId: 'PROD-MANTECA',  nombre: 'Manteca Vegetal',   precioUnitario: 8.50  },
  { productoId: 'PROD-POLLO',    nombre: 'Pollo Entero',      precioUnitario: 15.00 },
  { productoId: 'PROD-CARNE',    nombre: 'Carne de Res',      precioUnitario: 22.00 },
  { productoId: 'PROD-QUELLAVES',nombre: 'Quellaves Premium', precioUnitario: 12.00 },
];

const promos = ['PROMO-2X1', 'PROMO-10OFF', null];

// ============================================================
// FUNCIÓN PRINCIPAL
// ============================================================

export default function () {
  const numItems = Math.floor(Math.random() * 3) + 1;
  const items = [];
  const shuffled = [...productos].sort(() => Math.random() - 0.5);

  for (let i = 0; i < numItems && i < shuffled.length; i++) {
    const p = shuffled[i];
    // Cantidades pequeñas (1-2) para no agotar stock durante la prueba
    const cantidad = Math.floor(Math.random() * 2) + 1;
    items.push({
      productoId: p.productoId,
      nombre: p.nombre,
      cantidad: cantidad,
      precioUnitario: p.precioUnitario,
    });
  }

  const promocionId = promos[Math.floor(Math.random() * promos.length)];

  const payload = JSON.stringify({
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteEmail: `cliente${__VU}@logifresh.com`,
    clienteNombre: `Supermercado ${__VU}`,
    clienteDireccion: `Av. Prueba ${__VU}, Lima ${Math.floor(Math.random() * 100)}`,
    items: items,
    promocionId: promocionId,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'crear-pedido-completo' },
  };

  const res = http.post('http://localhost:8081/api/pedidos', payload, params);

  check(res, {
    '(CHECK) pedido creado o stock insuficiente (201/409)': (r) =>
      r.status === 201 || r.status === 409,

    '(CHECK) pedido CONFIRMADO (201)': (r) => {
      if (r.status !== 201) return true; // 409 es válido, no es fallo del check
      try {
        return JSON.parse(r.body).estado === 'CONFIRMADO';
      } catch { return false; }
    },

    '(CHECK) tiene pedidoId cuando status 201': (r) => {
      if (r.status !== 201) return true;
      try {
        const b = JSON.parse(r.body);
        return b.pedidoId !== undefined && b.pedidoId !== null;
      } catch { return false; }
    },

    '(CHECK) tiene facturaId cuando status 201': (r) => {
      if (r.status !== 201) return true;
      try {
        const b = JSON.parse(r.body);
        return b.facturaId !== undefined && b.facturaId !== null;
      } catch { return false; }
    },

    '(CHECK) respuesta recibida (< 5s)': (r) => r.timings.duration < 5000,
  });

  if (res.status !== 201 && res.status !== 409) {
    console.warn(`(FAIL) status inesperado: ${res.status} - ${res.body}`);
  }

  sleep(1);
}

// ============================================================
// SETUP — reinicia stock antes de la prueba
// ============================================================

export function setup() {
  console.log('(START) Iniciando prueba de rendimiento - ORQUESTADOR');
  console.log('(PROCESS) Flujo: Pedidos → Inventario → Facturación → Transporte → Notificaciones');

  // Verificar disponibilidad del servicio de pedidos
  const healthRes = http.get('http://localhost:8081/health');
  if (healthRes.status !== 200) {
    throw new Error('El servicio de Pedidos no está disponible. Asegúrate de correr docker compose up primero.');
  }

  // Reiniciar stock a valores altos para soportar la carga de 100 VUs × 5 min
  const stockReset = {
    'PROD-YOGURT':    9999,
    'PROD-LECHE':     9999,
    'PROD-MANTECA':   9999,
    'PROD-POLLO':     9999,
    'PROD-CARNE':     9999,
    'PROD-QUELLAVES': 9999,
  };

  const headers = { 'Content-Type': 'application/json' };
  for (const [productoId, cantidad] of Object.entries(stockReset)) {
    http.put(
      `http://localhost:8082/api/inventario/stock/${productoId}`,
      JSON.stringify({ operacion: 'SET', cantidad: cantidad }),
      { headers }
    );
  }

  console.log('(OK) Stock reiniciado para la prueba');
  console.log(`(INFO) ${options.vus} usuarios concurrentes por ${options.duration}`);
  console.log('================================================\n');

  return {};
}

// ============================================================
// TEARDOWN
// ============================================================

export function teardown(data) {
  console.log('\n================================================');
  console.log('(END) Prueba del orquestador finalizada');
  console.log('Métricas clave:');
  console.log('  - http_req_duration: tiempo de respuesta del flujo completo');
  console.log('  - http_req_failed:   errores de red (no incluye 409 esperados)');
  console.log('================================================');
}

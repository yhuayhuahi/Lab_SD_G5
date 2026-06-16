import http from 'k6/http';
import { sleep, check } from 'k6';

// ============================================================
// CONFIGURACIÓN DE LA PRUEBA — Servicio de Facturación
// ============================================================

export const options = {
  vus: 100,
  duration: '5m',
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% de peticiones < 2s
    http_req_failed: ['rate<0.01'],    // Menos del 1% de errores
  },
};

// ============================================================
// DATOS DE PRUEBA
// Este test va directo al servicio de Facturación (sin inventario)
// ============================================================

const productos = [
  { productoId: 'PROD-YOGURT',  nombre: 'Yogurt Natural',   precioUnitario: 3.50  },
  { productoId: 'PROD-LECHE',   nombre: 'Leche Entera',     precioUnitario: 5.20  },
  { productoId: 'PROD-MANTECA', nombre: 'Manteca Vegetal',  precioUnitario: 8.50  },
  { productoId: 'PROD-POLLO',   nombre: 'Pollo Entero',     precioUnitario: 15.00 },
  { productoId: 'PROD-CARNE',   nombre: 'Carne de Res',     precioUnitario: 22.00 },
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
    const cantidad = Math.floor(Math.random() * 5) + 1;
    items.push({
      productoId: p.productoId,
      nombre: p.nombre,
      cantidad: cantidad,
      precioUnitario: p.precioUnitario,
    });
  }

  // pedidoId único: timestamp + VU + iteración
  const pedidoId = `PED-${Date.now()}-${__VU}-${__ITER}`;
  const promocionId = promos[Math.floor(Math.random() * promos.length)];

  const payload = JSON.stringify({
    pedidoId: pedidoId,
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteNombre: `Supermercado ${__VU}`,
    clienteRuc: `20${String(10000000 + __VU).padStart(8, '0')}`,
    clienteDireccion: `Av. Prueba ${__VU}, Lima`,
    items: items,
    promocionId: promocionId,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'generar-factura' },
  };

  const res = http.post('http://localhost:8083/api/facturas/generar', payload, params);

  check(res, {
    '(CHECK) status 201 (factura creada)': (r) => r.status === 201,
    '(CHECK) tiene facturaId': (r) => {
      if (r.status !== 201) return false;
      try { return JSON.parse(r.body).facturaId !== undefined; }
      catch { return false; }
    },
    '(CHECK) tiempo < 1s': (r) => r.timings.duration < 1000,
  });

  sleep(1);
}

// ============================================================
// SETUP
// ============================================================

export function setup() {
  console.log('(START) Iniciando prueba de rendimiento - FACTURACIÓN');
  console.log(`(INFO) ${options.vus} usuarios concurrentes por ${options.duration}`);

  const healthRes = http.get('http://localhost:8083/health');
  if (healthRes.status !== 200) {
    throw new Error('El servicio de Facturación no está disponible.');
  }
  console.log('(OK) Servicio de Facturación disponible\n');
  return {};
}

// ============================================================
// TEARDOWN
// ============================================================

export function teardown(data) {
  console.log('\n================================================');
  console.log('(END) Prueba de Facturación finalizada');
  console.log('================================================');
}

import http from 'k6/http';
import { sleep, check } from 'k6';

// ============================================================
// CONFIGURACIÓN — prueba directa del endpoint /calcular y /generar
// ============================================================

export const options = {
  vus: 50,
  duration: '2m',
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    http_req_failed: ['rate<0.01'],
  },
};

// ============================================================
// FUNCIÓN PRINCIPAL
// ============================================================

export default function () {
  const headers = { 'Content-Type': 'application/json' };

  // pedidoId único con timestamp + VU + iteración para evitar DUPLICATE_INVOICE
  const pedidoId = `PED-${Date.now()}-${__VU}-${__ITER}`;

  const payload = JSON.stringify({
    pedidoId: pedidoId,
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteNombre: `Supermercado ${__VU}`,
    items: [
      { productoId: 'PROD-YOGURT', nombre: 'Yogurt', cantidad: 2, precioUnitario: 3.50 },
      { productoId: 'PROD-LECHE',  nombre: 'Leche',  cantidad: 1, precioUnitario: 5.20 },
    ],
    promocionId: 'PROMO-2X1',
  });

  const res = http.post(
    'http://localhost:8083/api/facturas/generar',
    payload,
    { headers, tags: { name: 'generar-factura' } }
  );

  check(res, {
    '(CHECK) status 201': (r) => r.status === 201,
    '(CHECK) tiene facturaId': (r) => {
      if (r.status !== 201) return false;
      try { return JSON.parse(r.body).facturaId !== undefined; }
      catch { return false; }
    },
    '(CHECK) descuento PROMO-2X1 aplicado': (r) => {
      if (r.status !== 201) return false;
      try {
        const b = JSON.parse(r.body);
        return b.descuentoTotal > 0 && b.promocionAplicada === 'PROMO-2X1';
      } catch { return false; }
    },
    '(CHECK) tiempo < 1.5s': (r) => r.timings.duration < 1500,
  });

  sleep(1);
}

// ============================================================
// SETUP
// ============================================================

export function setup() {
  console.log('(START) Prueba de Facturación con PROMO-2X1');

  const healthRes = http.get('http://localhost:8083/health');
  if (healthRes.status !== 200) {
    throw new Error('Servicio de Facturación no disponible.');
  }
  console.log('(OK) Servicio listo\n');
  return {};
}

export function teardown(data) {
  console.log('\n(END) Prueba finalizada');
}

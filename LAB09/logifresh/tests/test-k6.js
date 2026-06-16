import http from 'k6/http';
import { sleep, check } from 'k6';

// ============================================================
// CONFIGURACIÓN DE LA PRUEBA
// ============================================================

export const options = {
  vus: 100,               // 100 usuarios concurrentes
  duration: '5m',         // 5 minutos de ejecución
  thresholds: {
    // Umbrales de rendimiento (opcional)
    http_req_duration: ['p(95)<2000'], // 95% peticiones < 2s
    http_req_failed: ['rate<0.01'],    // Menos del 1% de errores
  },
};

// ============================================================
// DATOS DE PRUEBA (productos predefinidos)
// ============================================================

const productos = [
  { productoId: 'PROD-YOGURT', nombre: 'Yogurt Natural', precioUnitario: 3.50 },
  { productoId: 'PROD-LECHE', nombre: 'Leche Entera', precioUnitario: 5.20 },
  { productoId: 'PROD-MANTECA', nombre: 'Manteca Vegetal', precioUnitario: 8.50 },
  { productoId: 'PROD-POLLO', nombre: 'Pollo Entero', precioUnitario: 15.00 },
  { productoId: 'PROD-CARNE', nombre: 'Carne de Res', precioUnitario: 22.00 },
];

// ============================================================
// FUNCIÓN PRINCIPAL (se ejecuta por cada usuario virtual)
// ============================================================

export default function () {
  // 1. Seleccionar 1-3 productos aleatorios
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

  // 2. Construir payload
  const payload = JSON.stringify({
    pedidoId: `PED-${Date.now()}-${__VU}-${__ITER}`,
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteNombre: `Supermercado ${__VU}`,
    clienteRuc: `20${String(10000000 + __VU).padStart(8, '0')}`,
    clienteDireccion: `Av. Prueba ${__VU}, Lima`,
    items: items,
    promocionId: Math.random() > 0.5 ? 'PROMO-2X1' : null,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: { name: 'generar-factura' },
  };

  // 3. Enviar petición al servicio de facturación
  const url = 'http://localhost:8083/api/facturas/generar';
  const res = http.post(url, payload, params);

  // 4. Verificar respuesta
  const success = check(res, {
    'status 201': (r) => r.status === 201,
    'tiene facturaId': (r) => {
      try {
        return JSON.parse(r.body).facturaId !== undefined;
      } catch {
        return false;
      }
    },
    'tiempo < 1s': (r) => r.timings.duration < 1000,
  });

  // 5. Simular tiempo de espera entre peticiones
  sleep(1);
}

// ============================================================
// FUNCIÓN PARA LIMPIAR (opcional: se ejecuta al inicio)
// ============================================================

export function setup() {
  console.log('Iniciando prueba de rendimiento - LogiFresh');
  console.log(`${options.vus} usuarios concurrentes por ${options.duration}`);
  console.log('================================================\n');
  return {};
}

// ============================================================
// FUNCIÓN PARA REPORTE FINAL
// ============================================================

export function teardown(data) {
  console.log('\n================================================');
  console.log('Prueba finalizada');
  console.log('Revisa las métricas a continuación:');
}

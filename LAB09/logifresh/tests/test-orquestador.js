import http from 'k6/http';
import { sleep, check } from 'k6';

// ============================================================
// CONFIGURACIÓN DE LA PRUEBA
// ============================================================

export const options = {
  vus: 100,               // 100 usuarios concurrentes
  duration: '5m',         // 5 minutos de ejecución
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95% peticiones < 5s
    http_req_failed: ['rate<0.05'],    // Menos del 5% de errores
  },
};

// ============================================================
// DATOS DE PRUEBA
// ============================================================

const productos = [
  { productoId: 'PROD-YOGURT', nombre: 'Yogurt Natural', precioUnitario: 3.50 },
  { productoId: 'PROD-LECHE', nombre: 'Leche Entera', precioUnitario: 5.20 },
  { productoId: 'PROD-MANTECA', nombre: 'Manteca Vegetal', precioUnitario: 8.50 },
  { productoId: 'PROD-POLLO', nombre: 'Pollo Entero', precioUnitario: 15.00 },
  { productoId: 'PROD-CARNE', nombre: 'Carne de Res', precioUnitario: 22.00 },
  { productoId: 'PROD-QUELLAVES', nombre: 'Quellaves Premium', precioUnitario: 12.00 },
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
    const cantidad = Math.floor(Math.random() * 4) + 1;
    items.push({
      productoId: p.productoId,
      nombre: p.nombre,
      cantidad: cantidad,
      precioUnitario: p.precioUnitario,
    });
  }

  // 2. Construir payload para el orquestador (Servicio de Pedidos)
  const payload = JSON.stringify({
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteEmail: `cliente${__VU}@supermercado.com`,
    clienteNombre: `Supermercado ${__VU}`,
    clienteDireccion: `Av. Prueba ${__VU}, Lima ${Math.floor(Math.random() * 100)}`,
    items: items,
    promocionId: Math.random() > 0.5 ? 'PROMO-2X1' : null,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: { name: 'crear-pedido-completo' },
  };

  // 3. Enviar petición al ORQUESTADOR (Servicio de Pedidos)
  //    Este endpoint internamente llama a:
  //    - Inventario: /api/inventario/validar
  //    - Facturación: /api/facturas/calcular + /api/facturas/generar
  //    - Transporte: /api/transporte/asignar
  //    - Notificaciones: /api/notificaciones/enviar
  const url = 'http://localhost:8081/api/pedidos';
  const res = http.post(url, payload, params);

  // 4. Verificar respuesta
  const success = check(res, {
    '(CHECK) status 201 (pedido creado)': (r) => r.status === 201,
    '(CHECK) tiene pedidoId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.pedidoId !== undefined && body.pedidoId !== null;
      } catch {
        return false;
      }
    },
    '(CHECK) tiene facturaId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.facturaId !== undefined && body.facturaId !== null;
      } catch {
        return false;
      }
    },
    '(CHECK) tiene transportistaId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.transportistaId !== undefined && body.transportistaId !== null;
      } catch {
        return false;
      }
    },
    '(CHECK) estado es CONFIRMADO': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.estado === 'CONFIRMADO';
      } catch {
        return false;
      }
    },
    '(CHECK) respuesta rápida (< 2s)': (r) => r.timings.duration < 2000,
  });

  // 5. Si la petición falló, registrar información (opcional)
  if (!success) {
    console.warn(`(FAIL)  Petición fallida: ${res.status} - ${res.body}`);
  }

  // 6. Simular tiempo de espera entre peticiones
  sleep(1);
}

// ============================================================
// SETUP - Se ejecuta al inicio de la prueba
// ============================================================

export function setup() {
  console.log('(START)... Iniciando prueba de rendimiento - ORQUESTADOR (Servicio de Pedidos)');
  console.log('(PROCCESS)... Simulando flujo completo:');
  console.log('   Cliente → Pedidos → Inventario → Facturación → Transporte → Notificaciones');
  console.log(`👥 ${options.vus} usuarios concurrentes por ${options.duration}`);
  console.log('================================================\n');
  
  // Verificar que el servicio está disponible (opcional)
  const healthRes = http.get('http://localhost:8081/health');
  if (healthRes.status !== 200) {
    console.error('   El servicio de Pedidos no está disponible!');
    console.error(`   Status: ${healthRes.status}`);
    throw new Error('Servicio no disponible');
  }
  console.log('  Servicio de Pedidos disponible');
  
  return {};
}

// ============================================================
// TEARDOWN - Se ejecuta al final de la prueba
// ============================================================

export function teardown(data) {
  console.log('\n================================================');
  console.log('(>.<)     Prueba del orquestador finalizada');
  console.log('Revisa las métricas a continuación:');
  console.log('   🔍 Tiempo promedio incluye:');
  console.log('   - Validación de inventario (Redis)');
  console.log('   - Cálculo y generación de factura (PostgreSQL)');
  console.log('   - Asignación de transporte (Redis)');
  console.log('   - Envío de notificación (fire-and-forget)');
  console.log('================================================');
}

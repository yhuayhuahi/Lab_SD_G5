import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 50,
  duration: '2m',
};

export default function () {
  const payload = JSON.stringify({
    pedidoId: `PED-${Date.now()}-${__VU}`,
    clienteId: `CLI-${String(__VU).padStart(3, '0')}`,
    clienteNombre: `Supermercado ${__VU}`,
    items: [
      { productoId: 'PROD-YOGURT', nombre: 'Yogurt', cantidad: 2, precioUnitario: 3.50 },
      { productoId: 'PROD-LECHE', nombre: 'Leche', cantidad: 1, precioUnitario: 5.20 }
    ],
    promocionId: 'PROMO-2X1'
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post('http://localhost:8083/api/facturas/generar', payload, params);

  check(res, {
    'status 201': (r) => r.status === 201,
  });

  sleep(1);
}

import axios from 'axios';

const BASE_URL = process.env.INVENTARIO_URL || 'http://localhost:8082';
const TIMEOUT = 5000;

export interface ItemStock {
  productoId: string;
  cantidad: number;
}

export interface ValidarStockResponse {
  disponible: boolean;
  detalles: Array<{
    productoId: string;
    disponible: boolean;
    stockActual: number;
    solicitado: number;
    faltante?: number;
  }>;
  timestamp: string;
}

export async function validarStock(items: ItemStock[]): Promise<ValidarStockResponse> {
  const response = await axios.post(
    `${BASE_URL}/api/inventario/validar`,
    { items },
    { timeout: TIMEOUT }
  );
  return response.data;
}

export async function reservarStock(pedidoId: string, items: ItemStock[]): Promise<void> {
  await axios.post(
    `${BASE_URL}/api/inventario/reservar`,
    { pedidoId, items },
    { timeout: TIMEOUT }
  );
}

export async function liberarStock(pedidoId: string, items: ItemStock[]): Promise<void> {
  await axios.post(
    `${BASE_URL}/api/inventario/liberar`,
    { pedidoId, items },
    { timeout: TIMEOUT }
  );
}

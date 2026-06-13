import axios from 'axios';

const BASE_URL = process.env.FACTURACION_URL || 'http://localhost:8083';
const TIMEOUT = 5000;

export interface ItemCalculo {
  productoId: string;
  cantidad: number;
  precioUnitario: number;
}

export interface ItemFactura extends ItemCalculo {
  nombre: string;
  descuentoAplicado: number;
}

export interface CalculoResponse {
  subtotal: number;
  descuento: number;
  total: number;
  detalleDescuento: string;
  promocionAplicada: string | null;
  timestamp: string;
}

export interface FacturaGenerada {
  facturaId: string;
  pedidoId: string;
  clienteId: string;
  fechaEmision: string;
  subtotal: number;
  descuentoTotal: number;
  total: number;
  igv: number;
  totalConIgv: number;
  promocionAplicada: string | null;
  estado: string;
}

export async function calcularFactura(
  items: ItemCalculo[],
  promocionId?: string
): Promise<CalculoResponse> {
  const response = await axios.post(
    `${BASE_URL}/api/facturas/calcular`,
    { items, promocionId },
    { timeout: TIMEOUT }
  );
  return response.data;
}

export async function generarFactura(data: {
  pedidoId: string;
  clienteId: string;
  clienteNombre: string;
  clienteRuc?: string;
  clienteDireccion?: string;
  items: ItemFactura[];
  subtotal: number;
  descuentoTotal: number;
  total: number;
  promocionAplicada?: string | null;
}): Promise<FacturaGenerada> {
  const response = await axios.post(
    `${BASE_URL}/api/facturas/generar`,
    data,
    { timeout: TIMEOUT }
  );
  return response.data;
}

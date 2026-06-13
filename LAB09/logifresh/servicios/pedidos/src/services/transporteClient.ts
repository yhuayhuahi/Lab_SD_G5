import axios from 'axios';

const BASE_URL = process.env.TRANSPORTE_URL || 'http://localhost:8084';
const TIMEOUT = 5000;

export interface TransporteAsignado {
  transportistaId: string;
  pedidoId: string;
  estado: string;
  tiempoEstimadoMin: number;
  distanciaKm: number;
  fechaAsignacion: string;
  conductor: {
    nombre: string;
    telefono: string;
  };
  placa: string;
}

export async function asignarTransporte(
  pedidoId: string,
  clienteDireccion: string,
  prioridad: string = 'NORMAL'
): Promise<TransporteAsignado> {
  const response = await axios.post(
    `${BASE_URL}/api/transporte/asignar`,
    {
      pedidoId,
      clienteDireccion,
      tipoProducto: 'REFRIGERADO',
      prioridad,
    },
    { timeout: TIMEOUT }
  );
  return response.data;
}

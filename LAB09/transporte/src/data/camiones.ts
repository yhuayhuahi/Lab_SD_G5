export type EstadoCamion = 'DISPONIBLE' | 'OCUPADO' | 'MANTENIMIENTO';

export type TipoCamion = 'REFRIGERADO' | 'SECO';

export interface UbicacionCamion {
  lat: number;
  lng: number;
  direccion: string;
}

export interface ConductorCamion {
  nombre: string;
  telefono: string;
}

export interface Camion {
  transportistaId: string;
  placa: string;
  tipo: TipoCamion;
  capacidadKg: number;
  estado: EstadoCamion;
  conductor: ConductorCamion;
  ubicacionActual: UbicacionCamion;
  distanciaKm: number;
  pedidoActual?: string;
}

export const camionesIniciales: Camion[] = [
  {
    transportistaId: 'CAM-42',
    placa: 'ABI-789',
    tipo: 'REFRIGERADO',
    capacidadKg: 2500,
    estado: 'DISPONIBLE',
    conductor: { nombre: 'Carlos Mendoza', telefono: '+51987654321' },
    ubicacionActual: { lat: -12.0464, lng: -77.0428, direccion: 'Callao - Terminal Logístico' },
    distanciaKm: 12.5
  },
  {
    transportistaId: 'CAM-101',
    placa: 'ABC-123',
    tipo: 'REFRIGERADO',
    capacidadKg: 3500,
    estado: 'OCUPADO',
    conductor: { nombre: 'María Torres', telefono: '+51984567812' },
    ubicacionActual: { lat: -12.0831, lng: -77.0619, direccion: 'San Miguel - En ruta' },
    distanciaKm: 18.3,
    pedidoActual: 'PED-20241119-045'
  },
  {
    transportistaId: 'CAM-202',
    placa: 'DEF-456',
    tipo: 'REFRIGERADO',
    capacidadKg: 2800,
    estado: 'DISPONIBLE',
    conductor: { nombre: 'José Ramírez', telefono: '+51991234567' },
    ubicacionActual: { lat: -12.0702, lng: -77.0437, direccion: 'Lima Norte - Patio central' },
    distanciaKm: 9.8
  },
  {
    transportistaId: 'CAM-303',
    placa: 'GHI-321',
    tipo: 'SECO',
    capacidadKg: 4000,
    estado: 'DISPONIBLE',
    conductor: { nombre: 'Ana López', telefono: '+51993456789' },
    ubicacionActual: { lat: -12.0911, lng: -77.0482, direccion: 'San Isidro - Base operativa' },
    distanciaKm: 14.1
  },
  {
    transportistaId: 'CAM-404',
    placa: 'JKL-654',
    tipo: 'REFRIGERADO',
    capacidadKg: 2200,
    estado: 'MANTENIMIENTO',
    conductor: { nombre: 'Roberto Silva', telefono: '+51997654321' },
    ubicacionActual: { lat: -12.0569, lng: -77.0293, direccion: 'Ate - Taller de mantenimiento' },
    distanciaKm: 0
  },
  {
    transportistaId: 'CAM-505',
    placa: 'MNO-987',
    tipo: 'REFRIGERADO',
    capacidadKg: 3000,
    estado: 'OCUPADO',
    conductor: { nombre: 'Lucía Pérez', telefono: '+51981234567' },
    ubicacionActual: { lat: -12.1145, lng: -77.0356, direccion: 'Surquillo - En ruta' },
    distanciaKm: 16.4,
    pedidoActual: 'PED-20241119-046'
  },
  {
    transportistaId: 'CAM-606',
    placa: 'PQR-258',
    tipo: 'SECO',
    capacidadKg: 4500,
    estado: 'OCUPADO',
    conductor: { nombre: 'Fernando Castillo', telefono: '+51982345678' },
    ubicacionActual: { lat: -12.0378, lng: -77.0812, direccion: 'Magdalena - Ruta principal' },
    distanciaKm: 21.7,
    pedidoActual: 'PED-20241119-047'
  },
  {
    transportistaId: 'CAM-707',
    placa: 'STU-741',
    tipo: 'SECO',
    capacidadKg: 3800,
    estado: 'DISPONIBLE',
    conductor: { nombre: 'Claudia Fernández', telefono: '+51986543210' },
    ubicacionActual: { lat: -12.0998, lng: -77.0112, direccion: 'Lince - Centro de distribución' },
    distanciaKm: 7.6
  },
  {
    transportistaId: 'CAM-808',
    placa: 'VWX-963',
    tipo: 'REFRIGERADO',
    capacidadKg: 2600,
    estado: 'OCUPADO',
    conductor: { nombre: 'Jorge Medina', telefono: '+51989876543' },
    ubicacionActual: { lat: -12.1211, lng: -77.0299, direccion: 'Barranco - Última milla' },
    distanciaKm: 11.2,
    pedidoActual: 'PED-20241119-048'
  }
];
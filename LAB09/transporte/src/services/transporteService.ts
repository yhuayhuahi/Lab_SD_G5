import {
  camionesIniciales,
  type Camion,
  type ConductorCamion,
  type EstadoCamion,
  type TipoCamion,
  type UbicacionCamion
} from '../data/camiones';

export type EstadoTransporte = 'ASIGNADO' | 'EN_RUTA' | 'ENTREGADO' | 'CANCELADO';

export interface TransporteAsignado {
  transportistaId: string;
  pedidoId: string;
  estado: EstadoTransporte;
  tiempoEstimadoMin: number;
  distanciaKm: number;
  fechaAsignacion: string;
  conductor: ConductorCamion;
  placa: string;
}

export interface TransporteEstado {
  pedidoId: string;
  transportistaId: string;
  estado: EstadoTransporte;
  ubicacionActual: UbicacionCamion;
  actualizacion: string;
  tiempoEstimadoRestanteMin: number;
  conductor: ConductorCamion;
}

export interface TransporteActualizado {
  pedidoId: string;
  transportistaId: string;
  estadoAnterior: EstadoTransporte;
  estadoActual: EstadoTransporte;
  fechaActualizacion: string;
  tiempoTotalMin: number;
}

interface TransporteRecord {
  pedidoId: string;
  transportistaId: string;
  estado: EstadoTransporte;
  tiempoEstimadoMin: number;
  distanciaKm: number;
  fechaAsignacion: string;
  fechaActualizacion: string;
  conductor: ConductorCamion;
  placa: string;
  ubicacionActual: UbicacionCamion;
}

interface FiltroCamiones {
  estado?: EstadoCamion;
  tipo?: TipoCamion;
}

export class TransporteError extends Error {
  constructor(
    public readonly code: string,
    message: string,
    public readonly details?: Record<string, unknown>
  ) {
    super(message);
    this.name = 'TransporteError';
  }
}

const camiones = new Map<string, Camion>(camionesIniciales.map((camion) => [camion.transportistaId, { ...camion }]));
const transportes = new Map<string, TransporteRecord>();
let siguienteCamion = 0;

function obtenerIdsCamiones(): string[] {
  return Array.from(camiones.keys());
}

function obtenerCamionDisponible(): Camion | null {
  const idsCamiones = obtenerIdsCamiones();

  if (idsCamiones.length === 0) {
    return null;
  }

  for (let intento = 0; intento < idsCamiones.length; intento += 1) {
    const indice = (siguienteCamion + intento) % idsCamiones.length;
    const camion = camiones.get(idsCamiones[indice]);

    if (camion && camion.estado === 'DISPONIBLE') {
      siguienteCamion = (indice + 1) % idsCamiones.length;
      return camion;
    }
  }

  return null;
}

function liberarCamion(transportistaId: string): void {
  const camion = camiones.get(transportistaId);

  if (!camion || camion.estado === 'MANTENIMIENTO') {
    return;
  }

  camion.estado = 'DISPONIBLE';
  delete camion.pedidoActual;
}

function ocuparCamion(transportistaId: string, pedidoId: string): void {
  const camion = camiones.get(transportistaId);

  if (!camion) {
    return;
  }

  camion.estado = 'OCUPADO';
  camion.pedidoActual = pedidoId;
}

function normalizarEstado(estado: string): EstadoTransporte | null {
  const valor = estado.trim().toUpperCase();

  if (valor === 'ASIGNADO' || valor === 'EN_RUTA' || valor === 'ENTREGADO' || valor === 'CANCELADO') {
    return valor;
  }

  return null;
}

function estadoTerminal(estado: EstadoTransporte): boolean {
  return estado === 'ENTREGADO' || estado === 'CANCELADO';
}

function estadoPermitidoDesde(estadoActual: EstadoTransporte): EstadoTransporte[] {
  if (estadoActual === 'ASIGNADO') {
    return ['EN_RUTA', 'CANCELADO'];
  }

  if (estadoActual === 'EN_RUTA') {
    return ['ENTREGADO', 'CANCELADO'];
  }

  return [];
}

function calcularTiempoRestante(record: TransporteRecord): number {
  if (record.estado === 'ENTREGADO' || record.estado === 'CANCELADO') {
    return 0;
  }

  if (record.estado === 'EN_RUTA') {
    return Math.max(5, Math.round(record.tiempoEstimadoMin * 0.5));
  }

  return record.tiempoEstimadoMin;
}

function calcularTiempoTotalMin(fechaAsignacion: string, fechaActualizacion: string): number {
  const diferenciaMs = new Date(fechaActualizacion).getTime() - new Date(fechaAsignacion).getTime();
  return Math.max(0, Math.round(diferenciaMs / 60000));
}

function obtenerRegistroTransporte(pedidoId: string): TransporteRecord | null {
  return transportes.get(pedidoId) ?? null;
}

export function listarCamiones(filtros: FiltroCamiones = {}): Array<{
  transportistaId: string;
  placa: string;
  tipo: TipoCamion;
  capacidadKg: number;
  estado: EstadoCamion;
  conductor: string;
  pedidoActual?: string;
  ubicacion: string;
}> {
  return Array.from(camiones.values())
    .filter((camion) => (filtros.estado ? camion.estado === filtros.estado : true))
    .filter((camion) => (filtros.tipo ? camion.tipo === filtros.tipo : true))
    .map((camion) => ({
      transportistaId: camion.transportistaId,
      placa: camion.placa,
      tipo: camion.tipo,
      capacidadKg: camion.capacidadKg,
      estado: camion.estado,
      conductor: camion.conductor.nombre,
      pedidoActual: camion.pedidoActual,
      ubicacion: camion.ubicacionActual.direccion
    }));
}

export function asignarCamion(pedidoId: string, tiempoEstimadoMin: number): TransporteAsignado {
  const transporteExistente = obtenerRegistroTransporte(pedidoId);

  if (transporteExistente) {
    return {
      transportistaId: transporteExistente.transportistaId,
      pedidoId: transporteExistente.pedidoId,
      estado: transporteExistente.estado,
      tiempoEstimadoMin: transporteExistente.tiempoEstimadoMin,
      distanciaKm: transporteExistente.distanciaKm,
      fechaAsignacion: transporteExistente.fechaAsignacion,
      conductor: transporteExistente.conductor,
      placa: transporteExistente.placa
    };
  }

  const camion = obtenerCamionDisponible();

  if (!camion) {
    throw new TransporteError('NO_TRUCKS_AVAILABLE', 'No hay camiones disponibles en este momento', {
      tiempoEstimadoEsperaMin: 15
    });
  }

  ocuparCamion(camion.transportistaId, pedidoId);

  const fechaAsignacion = new Date().toISOString();

  const registro: TransporteRecord = {
    pedidoId,
    transportistaId: camion.transportistaId,
    estado: 'ASIGNADO',
    tiempoEstimadoMin,
    distanciaKm: camion.distanciaKm,
    fechaAsignacion,
    fechaActualizacion: fechaAsignacion,
    conductor: camion.conductor,
    placa: camion.placa,
    ubicacionActual: camion.ubicacionActual
  };

  transportes.set(pedidoId, registro);

  return {
    transportistaId: camion.transportistaId,
    pedidoId,
    estado: 'ASIGNADO',
    tiempoEstimadoMin,
    distanciaKm: camion.distanciaKm,
    fechaAsignacion,
    conductor: camion.conductor,
    placa: camion.placa
  };
}

export function obtenerEstado(pedidoId: string): TransporteEstado | null {
  const transporte = obtenerRegistroTransporte(pedidoId);

  if (!transporte) {
    return null;
  }

  return {
    pedidoId: transporte.pedidoId,
    transportistaId: transporte.transportistaId,
    estado: transporte.estado,
    ubicacionActual: transporte.ubicacionActual,
    actualizacion: transporte.fechaActualizacion,
    tiempoEstimadoRestanteMin: calcularTiempoRestante(transporte),
    conductor: transporte.conductor
  };
}

export function actualizarEstado(pedidoId: string, estadoTexto: string): TransporteActualizado {
  const transporte = obtenerRegistroTransporte(pedidoId);

  if (!transporte) {
    throw new TransporteError('NOT_FOUND', 'No hay registro de transporte para el pedido');
  }

  const nuevoEstado = normalizarEstado(estadoTexto);

  if (!nuevoEstado) {
    throw new TransporteError('VALIDATION_ERROR', 'El campo estado debe ser ASIGNADO, EN_RUTA, ENTREGADO o CANCELADO');
  }

  const estadosPermitidos = estadoPermitidoDesde(transporte.estado);

  if (nuevoEstado === transporte.estado || !estadosPermitidos.includes(nuevoEstado)) {
    throw new TransporteError(
      'INVALID_STATE_TRANSITION',
      generarMensajeTransicion(transporte.estado, nuevoEstado),
      { estadoActual: transporte.estado, estadosPermitidos }
    );
  }

  const estadoAnterior = transporte.estado;
  const fechaActualizacion = new Date().toISOString();
  transporte.estado = nuevoEstado;
  transporte.fechaActualizacion = fechaActualizacion;

  if (estadoTerminal(nuevoEstado)) {
    liberarCamion(transporte.transportistaId);
  } else {
    ocuparCamion(transporte.transportistaId, pedidoId);
  }

  transportes.set(pedidoId, transporte);

  return {
    pedidoId,
    transportistaId: transporte.transportistaId,
    estadoAnterior,
    estadoActual: nuevoEstado,
    fechaActualizacion,
    tiempoTotalMin: calcularTiempoTotalMin(transporte.fechaAsignacion, fechaActualizacion)
  };
}

function generarMensajeTransicion(estadoActual: EstadoTransporte, nuevoEstado: EstadoTransporte): string {
  const permitidos = estadoPermitidoDesde(estadoActual).join(', ');
  return `No se puede cambiar de estado ${estadoActual} a ${nuevoEstado}. Debe pasar por ${permitidos || 'ninguno'}`;
}
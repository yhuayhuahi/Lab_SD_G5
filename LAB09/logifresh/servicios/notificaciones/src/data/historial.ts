export type EstadoNotificacion = 'ENVIADO' | 'FALLIDO' | 'PENDIENTE';

export interface Notificacion {
  notificacionId: string;
  email: string;
  asunto: string;
  mensaje: string;
  pedidoId?: string;
  template?: string;
  datosAdicionales?: Record<string, unknown>;
  estado: EstadoNotificacion;
  fechaEnvio: string;
  intentos: number;
  ultimoError: string | null;
}

export const historialNotificaciones: Notificacion[] = [];

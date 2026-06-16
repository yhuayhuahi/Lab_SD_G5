import { historialNotificaciones, Notificacion } from '../data/historial.js';
import logger from '../utils/logger.js';

export interface EnviarCorreoInput {
  email: string;
  asunto: string;
  mensaje: string;
  pedidoId?: string;
  template?: string;
  datosAdicionales?: Record<string, unknown>;
}

function generarNotificacionId(): string {
  const sufijo = Math.floor(Math.random() * 10000);
  return `NOT-${Date.now()}-${sufijo}`;
}

function falloAleatorioHabilitado(): boolean {
  return process.env.ENABLE_RANDOM_FAILURE === 'true';
}

function debeFallarAleatoriamente(): boolean {
  const tasa = Number(process.env.RANDOM_FAILURE_RATE ?? 0.15);
  return falloAleatorioHabilitado() && Math.random() < tasa;
}

export async function enviarCorreo(
  input: EnviarCorreoInput,
  opciones?: { forzarFallo?: boolean }
): Promise<Notificacion> {
  const notificacionId = generarNotificacionId();
  const fechaEnvio = new Date().toISOString();

  if (opciones?.forzarFallo || debeFallarAleatoriamente()) {
    const fallida: Notificacion = {
      notificacionId,
      email: input.email,
      asunto: input.asunto,
      mensaje: input.mensaje,
      pedidoId: input.pedidoId,
      template: input.template,
      datosAdicionales: input.datosAdicionales,
      estado: 'FALLIDO',
      fechaEnvio,
      intentos: 1,
      ultimoError: 'SMTP_CONNECTION_REFUSED',
    };

    historialNotificaciones.unshift(fallida);
    if (historialNotificaciones.length > 500) historialNotificaciones.pop();
    logger.error(`Fallo al enviar correo ${notificacionId} a ${input.email}`);
    return fallida;
  }

  const enviada: Notificacion = {
    notificacionId,
    email: input.email,
    asunto: input.asunto,
    mensaje: input.mensaje,
    pedidoId: input.pedidoId,
    template: input.template,
    datosAdicionales: input.datosAdicionales,
    estado: 'ENVIADO',
    fechaEnvio,
    intentos: 1,
    ultimoError: null,
  };

  historialNotificaciones.unshift(enviada);
  if (historialNotificaciones.length > 500) historialNotificaciones.pop();

  logger.info(`Correo simulado enviado a ${input.email}`);
  logger.info(`Asunto: ${input.asunto}`);
  logger.info(`Mensaje: ${input.mensaje}`);

  return enviada;
}

export function esperar(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

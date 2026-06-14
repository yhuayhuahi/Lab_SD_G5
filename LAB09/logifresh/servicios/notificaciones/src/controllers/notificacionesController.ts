import { Request, Response } from 'express';
import { historialNotificaciones } from '../data/historial.js';
import { enviarCorreo, esperar, EnviarCorreoInput } from '../services/emailService.js';

function validarBody(body: Partial<EnviarCorreoInput>): string | null {
  if (!body.email) return 'El campo email es requerido';
  if (!body.asunto) return 'El campo asunto es requerido';
  if (!body.mensaje) return 'El campo mensaje es requerido';
  return null;
}

export async function enviarNotificacion(req: Request, res: Response): Promise<void> {
  const body = req.body as Partial<EnviarCorreoInput>;

  const errorValidacion = validarBody(body);
  if (errorValidacion) {
    res.status(400).json({
      error: 'VALIDATION_ERROR',
      mensaje: errorValidacion,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  const notificacion = await enviarCorreo(body as EnviarCorreoInput);

  if (notificacion.estado === 'FALLIDO') {
    res.status(500).json({
      error: 'EMAIL_SERVICE_FAILED',
      mensaje: 'No se pudo enviar el correo. El servicio de email no responde',
      codigoError: notificacion.ultimoError,
      notificacionId: notificacion.notificacionId,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  res.status(200).json({
    success: true,
    notificacionId: notificacion.notificacionId,
    email: notificacion.email,
    estado: notificacion.estado,
    fechaEnvio: notificacion.fechaEnvio,
    mensaje: 'Correo enviado exitosamente (simulado)',
  });
}

export function obtenerEstadoNotificacion(req: Request, res: Response): void {
  const { id } = req.params;

  const notificacion = historialNotificaciones.find((n) => n.notificacionId === id);

  if (!notificacion) {
    res.status(404).json({
      error: 'NOT_FOUND',
      mensaje: `No existe notificación con ID: ${id}`,
      timestamp: new Date().toISOString(),
    });
    return;
  }

  res.status(200).json({
    notificacionId: notificacion.notificacionId,
    email: notificacion.email,
    asunto: notificacion.asunto,
    estado: notificacion.estado,
    fechaEnvio: notificacion.fechaEnvio,
    intentos: notificacion.intentos,
    ultimoError: notificacion.ultimoError,
  });
}

export function listarHistorial(req: Request, res: Response): void {
  const email = req.query.email as string | undefined;
  const estado = req.query.estado as string | undefined;
  const limit = Number(req.query.limit ?? 10);
  const offset = Number(req.query.offset ?? 0);

  let resultado = [...historialNotificaciones];

  if (email) {
    resultado = resultado.filter((n) => n.email === email);
  }

  if (estado) {
    resultado = resultado.filter((n) => n.estado === estado);
  }

  const paginado = resultado.slice(offset, offset + limit);

  res.status(200).json({
    total: resultado.length,
    limit,
    offset,
    notificaciones: paginado.map((n) => ({
      notificacionId: n.notificacionId,
      email: n.email,
      asunto: n.asunto,
      estado: n.estado,
      fechaEnvio: n.fechaEnvio,
      ...(n.ultimoError ? { error: n.ultimoError } : {}),
    })),
  });
}

export async function simularFallo(req: Request, res: Response): Promise<void> {
  const body = req.body as Partial<EnviarCorreoInput>;

  const input: EnviarCorreoInput = {
    email: body.email ?? 'cliente@logifresh.com',
    asunto: body.asunto ?? 'Fallo simulado',
    mensaje: body.mensaje ?? 'Fallo simulado para pruebas de integración',
    pedidoId: body.pedidoId,
    template: body.template,
    datosAdicionales: body.datosAdicionales,
  };

  await enviarCorreo(input, { forzarFallo: true });

  res.status(500).json({
    error: 'SIMULATED_FAILURE',
    mensaje: 'Fallo simulado para pruebas de integración',
    timestamp: new Date().toISOString(),
  });
}

export async function simularLentitud(req: Request, res: Response): Promise<void> {
  await esperar(5000);
  await enviarNotificacion(req, res);
}

import axios from 'axios';
import logger from '../utils/logger.js';

const BASE_URL = process.env.NOTIFICACIONES_URL || 'http://localhost:8085';

interface NotificacionData {
  email: string;
  asunto: string;
  mensaje: string;
  pedidoId: string;
  template: string;
  datosAdicionales?: Record<string, unknown>;
}

// Fire-and-forget: no bloquea el flujo principal
export function enviarNotificacion(data: NotificacionData): void {
  axios
    .post(`${BASE_URL}/api/notificaciones/enviar`, data, { timeout: 10000 })
    .then((res) => {
      logger.info(`Notificación enviada para pedido ${data.pedidoId}: ${res.data.notificacionId}`);
    })
    .catch((err: unknown) => {
      const msg = err instanceof Error ? err.message : String(err);
      logger.warn(`Fallo al enviar notificación para ${data.pedidoId} (no crítico): ${msg}`);
    });
}

import express from 'express';
import cors from 'cors';
import {
  enviarNotificacion,
  obtenerEstadoNotificacion,
  listarHistorial,
  simularFallo,
  simularLentitud,
} from './controllers/notificacionesController.js';

const app = express();

app.use(cors());
app.use(express.json());

app.get('/health', (_req, res) => {
  res.json({
    status: 'OK',
    service: 'notificaciones',
    timestamp: new Date().toISOString(),
  });
});

app.post('/api/notificaciones/enviar', enviarNotificacion);
app.get('/api/notificaciones/estado/:id', obtenerEstadoNotificacion);
app.get('/api/notificaciones/historial', listarHistorial);
app.post('/api/notificaciones/test/fallo', simularFallo);
app.post('/api/notificaciones/test/lento', simularLentitud);

// Ruta adicional por compatibilidad con indicaciones.md
app.post('/api/notificaciones/enviar/lento', simularLentitud);

export default app;

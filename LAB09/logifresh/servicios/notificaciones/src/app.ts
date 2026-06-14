import express, { NextFunction, Request, Response } from 'express';
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

// agregamos una ruta adicional
app.post('/api/notificaciones/enviar/lento', simularLentitud);

// para las rutas que no encontramos
app.use((req: Request, res: Response) => {
  res.status(404).json({
    error: 'ROUTE_NOT_FOUND',
    mensaje: `La ruta ${req.method} ${req.originalUrl} no existe en el servicio de notificaciones`,
    timestamp: new Date().toISOString(),
  });
});

// esto funciona como un middleware, si ocurriera cualquier error aqui lo capturamos
app.use((error: Error, _req: Request, res: Response, _next: NextFunction) => {
  res.status(500).json({
    error: 'INTERNAL_SERVER_ERROR',
    mensaje: 'Ocurrió un error inesperado en el servicio de notificaciones',
    detalle: process.env.NODE_ENV === 'development' ? error.message : undefined,
    timestamp: new Date().toISOString(),
  });
});

export default app;
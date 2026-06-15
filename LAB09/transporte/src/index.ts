import 'dotenv/config';
import { createApp } from './app';
import { logError, logInfo } from './utils/logger';

const port = Number(process.env.PORT ?? '8084');
const app = createApp();

app.listen(port, () => {
  logInfo(`Servicio de transporte escuchando en el puerto ${port}`);
});

process.on('unhandledRejection', (reason) => {
  logError('Unhandled rejection', { reason });
});
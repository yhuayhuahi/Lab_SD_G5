import dotenv from 'dotenv';
import app from './app.js';
import logger from './utils/logger.js';

dotenv.config();

const PORT = Number(process.env.PORT) || 8085;

app.listen(PORT, () => {
  logger.info(`Servicio de Notificaciones ejecutándose en puerto ${PORT}`);
});

import 'dotenv/config';
import mongoose from 'mongoose';
import app from './app.js';
import logger from './utils/logger.js';

const PORT = parseInt(process.env.PORT ?? '8081', 10);
const MONGO_URI = process.env.MONGO_URI ?? 'mongodb://localhost:27017/pedidos';

async function start() {
  try {
    await mongoose.connect(MONGO_URI);
    logger.info('Conectado a MongoDB');

    app.listen(PORT, () => {
      logger.info(`Servicio de Pedidos corriendo en http://localhost:${PORT}`);
    });
  } catch (error) {
    logger.error('Error al iniciar el servicio', error);
    process.exit(1);
  }
}

start();

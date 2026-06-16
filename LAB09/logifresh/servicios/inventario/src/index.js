require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { connectRedis } = require('./config/redis');
const { inicializarStock, verificarConexion } = require('./services/inventarioService');
const inventarioRouter = require('./routes/inventario');
const errorHandler = require('./middlewares/errorHandler');

const app = express();
const PORT = process.env.PORT || 8082;

// Middlewares globales
app.use(cors());
app.use(express.json());

// Health check — verifica conectividad real con Redis
app.get('/health', async (req, res) => {
  try {
    const redisOk = await verificarConexion();
    const estado = redisOk ? 'OK' : 'DEGRADADO';
    res.status(redisOk ? 200 : 503).json({
      servicio: 'inventario',
      estado,
      dependencias: { redis: redisOk ? 'conectado' : 'desconectado' },
      timestamp: new Date().toISOString(),
    });
  } catch (err) {
    res.status(503).json({
      servicio: 'inventario',
      estado: 'ERROR',
      dependencias: { redis: 'error' },
      error: err.message,
      timestamp: new Date().toISOString(),
    });
  }
});

// Rutas
app.use('/api/inventario', inventarioRouter);

// Manejo de rutas no encontradas
app.use((req, res) => {
  res.status(404).json({
    error: 'NOT_FOUND',
    mensaje: `Ruta no encontrada: ${req.method} ${req.originalUrl}`,
    timestamp: new Date().toISOString(),
  });
});

// Manejo global de errores
app.use(errorHandler);

// Arranque del servidor
const start = async () => {
  try {
    await connectRedis();
    await inicializarStock();

    app.listen(PORT, () => {
      console.log(`[Inventario] Servidor corriendo en http://localhost:${PORT}`);
      console.log(`[Inventario] Health check: http://localhost:${PORT}/health`);
    });
  } catch (err) {
    console.error('[Inventario] Error al iniciar el servicio:', err.message);
    process.exit(1);
  }
};

start();
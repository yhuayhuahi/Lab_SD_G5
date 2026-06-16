require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { connectRedis } = require('./config/redis');
const { inicializarStock } = require('./services/inventarioService');
const inventarioRouter = require('./routes/inventario');
const errorHandler = require('./middlewares/errorHandler');

const app = express();
const PORT = process.env.PORT || 8082;

// Middlewares globales
app.use(cors());
app.use(express.json());

// Health check
app.get('/health', (req, res) => {
  res.status(200).json({
    servicio: 'inventario',
    estado: 'OK',
    timestamp: new Date().toISOString(),
  });
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
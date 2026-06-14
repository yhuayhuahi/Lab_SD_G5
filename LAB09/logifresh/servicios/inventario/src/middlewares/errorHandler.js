const errorHandler = (err, req, res, next) => {
  console.error('[Error]', err.message);

  return res.status(500).json({
    error: 'INTERNAL_ERROR',
    mensaje: 'Error interno del servidor',
    timestamp: new Date().toISOString(),
  });
};

module.exports = errorHandler;
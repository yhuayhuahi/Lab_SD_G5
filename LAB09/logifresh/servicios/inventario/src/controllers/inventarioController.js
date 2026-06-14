const { Router } = require('express');
const {
  validar,
  reservar,
  liberar,
  consultarStock,
  actualizarStockHandler,
} = require('../controllers/inventarioController');

const router = Router();

// POST /api/inventario/validar
router.post('/validar', validar);

// POST /api/inventario/reservar
router.post('/reservar', reservar);

// POST /api/inventario/liberar
router.post('/liberar', liberar);

// GET /api/inventario/stock/:productoId
router.get('/stock/:productoId', consultarStock);

// PUT /api/inventario/stock/:productoId
router.put('/stock/:productoId', actualizarStockHandler);

module.exports = router;
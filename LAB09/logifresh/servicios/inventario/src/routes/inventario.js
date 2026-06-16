const { Router } = require('express');
const {
  validar,
  reservar,
  liberar,
  consultarStock,
  actualizarStockHandler,
} = require('../controllers/inventarioController');

const router = Router();

router.post('/validar', validar);
router.post('/reservar', reservar);
router.post('/liberar', liberar);
router.get('/stock/:productoId', consultarStock);
router.put('/stock/:productoId', actualizarStockHandler);

module.exports = router;

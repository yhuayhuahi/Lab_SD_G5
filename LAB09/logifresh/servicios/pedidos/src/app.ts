import express from 'express';
import cors from 'cors';
import {
  createPedido,
  createPedidoLento,
  getPedido,
  cancelarPedido,
} from './controllers/pedidoController.js';

const app = express();

app.use(cors());
app.use(express.json());

app.get('/health', (_req, res) => {
  res.json({ status: 'OK', service: 'pedidos', timestamp: new Date().toISOString() });
});

// Ruta lenta registrada primero para que no sea capturada por /:id
app.post('/api/pedidos/test/lento', createPedidoLento);
app.post('/api/pedidos', createPedido);
app.get('/api/pedidos/:id', getPedido);
app.put('/api/pedidos/:id/cancelar', cancelarPedido);

export default app;

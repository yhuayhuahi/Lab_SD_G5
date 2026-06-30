import express, { json } from 'express'
import cors from 'cors'
import helmet from 'helmet'

import { InventoryController } from './controllers/inventory.controller.js'
import { OrderController } from './controllers/order.controller.js'
import { CustomerController } from './controllers/customer.controller.js'

const app = express()
const PORT = 3000

// midlewares
app.use(helmet())
app.use(cors())
app.use(json())

app.use((req, _, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url} - IP: ${req.ip}`);
  next();
});

// Endpoints
app.get('/health', (_, res) => {
  res.json({ 
    status: 'OK', 
    service: 'logimarket-api', 
    timestamp: new Date(),
    nodeVersion: process.version 
  });
});

app.get('/api/inventory', InventoryController.getAll)
app.get('/api/inventory/:id', InventoryController.getById)

app.post('/api/orders', OrderController.createOrder)
app.get('/api/orders', OrderController.getAll)

app.get('/api/customers', CustomerController.getAll)

app.listen(PORT, () => {
  console.log(`Server escuchando en http://localhost:${PORT}`)
})



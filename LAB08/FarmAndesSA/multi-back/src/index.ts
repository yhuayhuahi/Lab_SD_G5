import express, { json } from 'express';
import pool from './pool';
import cors from 'cors'; 
import * as queries from './queries';
import { config } from './config';
import { transferirProducto } from './coordinator';
import { PrepareRequest, PrepareResponse } from './types';

const app = express();
app.use(json());
app.use(cors());

// ============ ENDPOINTS PÚBLICOS ============

// Ver estado del nodo
app.get('/health', (req, res) => {
  res.json({
    city: config.city,
    status: 'healthy',
    db: config.dbName,
    timestamp: new Date().toISOString()
  });
});

// Ver inventario completo
app.get('/inventario', async (req, res) => {
  try {
    const inventario = await queries.getInventario();
    res.json({
      city: config.city,
      inventario
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Ver stock de un producto específico
app.get('/inventario/:producto', async (req, res) => {
  try {
    const stock = await queries.getStock(req.params.producto);
    res.json({
      producto: req.params.producto,
      stock: stock ?? 0,
      city: config.city
    });
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// Crear o actualizar producto
app.post('/inventario', async (req, res) => {
  try {
    const { producto, stock } = req.body;
    if (!producto) {
      return res.status(400).json({ error: 'Producto requerido' });
    }
    const item = await queries.crearProducto(producto, stock || 0);
    res.json(item);
  } catch (error: any) {
    res.status(500).json({ error: error.message });
  }
});

// ============ ENDPOINT DE TRANSFERENCIA (CLIENTE) ============

app.post('/transferir', async (req, res) => {
  try {
    const { producto, cantidad, destino } = req.body;
    
    // Validaciones
    if (!producto || !cantidad || !destino) {
      return res.status(400).json({
        success: false,
        message: 'Faltan campos: producto, cantidad, destino'
      });
    }
    
    if (cantidad <= 0) {
      return res.status(400).json({
        success: false,
        message: 'La cantidad debe ser mayor a 0'
      });
    }
    
    const destinoLower = destino.toLowerCase();
    if (!['arequipa', 'lima', 'cusco'].includes(destinoLower)) {
      return res.status(400).json({
        success: false,
        message: 'Destino inválido. Use: arequipa, lima, cusco'
      });
    }
    
    if (destinoLower === config.city) {
      return res.status(400).json({
        success: false,
        message: 'No puede transferir al mismo almacén'
      });
    }
    
    const result = await transferirProducto(producto, cantidad, destinoLower);
    res.status(result.success ? 200 : 400).json(result);
    
  } catch (error: any) {
    res.status(500).json({
      success: false,
      message: `Error interno: ${error.message}`
    });
  }
});

// ============ ENDPOINTS PARA 2PC (entre nodos) ============

// Prepare: destino recibe la solicitud de reserva
app.post('/api/2pc/prepare', async (req, res) => {
  const { producto, cantidad, origen, destino, transactionId }: PrepareRequest = req.body;
  const client = await pool.connect();
  
  console.log(`📥 [${transactionId}] Prepare recibido de ${origen} (soy ${config.city})`);
  
  try {
    await client.query('BEGIN');
    
    // Verificar/crear producto y preparar para suma
    const success = await queries.prepareSuma(client, producto, cantidad);
    
    if (!success) {
      await client.query('ROLLBACK');
      client.release();
      console.log(`❌ [${transactionId}] Prepare rechazado`);
      return res.json({
        success: false,
        message: 'No se pudo preparar el destino'
      } as PrepareResponse);
    }
    
    // No hacemos COMMIT aún, solo preparamos
    // Guardamos el estado en memoria o DB (por simplicidad, la transacción queda abierta)
    // En producción, deberías persistir el estado de la transacción
    
    console.log(`✅ [${transactionId}] Prepare OK, esperando COMMIT...`);
    
    // Devolvemos respuesta exitosa pero mantenemos la transacción abierta
    // (el COMMIT vendrá después)
    res.json({
      success: true,
      message: 'Destino preparado, esperando commit'
    } as PrepareResponse);
    
  } catch (error: any) {
    await client.query('ROLLBACK');
    client.release();
    console.log(`❌ [${transactionId}] Prepare error: ${error.message}`);
    res.json({
      success: false,
      message: error.message
    } as PrepareResponse);
  }
});

// Commit: destino confirma la suma
app.post('/api/2pc/commit', async (req, res) => {
  const { transactionId, producto, cantidad } = req.body;
  const client = await pool.connect();
  
  console.log(`📥 [${transactionId}] Commit recibido, sumando stock...`);
  
  try {
    await client.query('BEGIN');
    await queries.commitSuma(client, producto, cantidad);
    await client.query('COMMIT');
    client.release();
    
    console.log(`✅ [${transactionId}] Commit OK, stock actualizado`);
    res.json({ success: true });
    
  } catch (error: any) {
    await client.query('ROLLBACK');
    client.release();
    console.log(`❌ [${transactionId}] Commit error: ${error.message}`);
    res.status(500).json({ success: false, message: error.message });
  }
});

// Rollback: destino cancela (si algo falla)
app.post('/api/2pc/rollback', async (req, res) => {
  const { transactionId } = req.body;
  const client = await pool.connect();
  
  console.log(`📥 [${transactionId}] Rollback recibido`);
  
  try {
    await client.query('ROLLBACK');
    client.release();
    console.log(`✅ [${transactionId}] Rollback OK`);
    res.json({ success: true });
  } catch (error: any) {
    client.release();
    res.status(500).json({ success: false, message: error.message });
  }
});

// ============ INICIAR SERVIDOR ============

app.listen(config.port, () => {
  console.log(`🚀 Backend ${config.city} escuchando en puerto ${config.port}`);
});


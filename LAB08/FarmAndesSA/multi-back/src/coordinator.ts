import pool from './pool';
import { config } from './config';
import * as queries from './queries';
import { PrepareRequest, PrepareResponse } from './types';

// Generar ID único de transacción
function generateTransactionId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 10)}`;
}

// Obtener URL del peer por ciudad
function getPeerUrl(city: string): string {
  switch (city.toLowerCase()) {
    case 'arequipa': return config.peers.arequipa;
    case 'lima': return config.peers.lima;
    case 'cusco': return config.peers.cusco;
    default: throw new Error(`Ciudad desconocida: ${city}`);
  }
}

// Helper para fetch con timeout
async function fetchWithTimeout(
  url: string,
  options: RequestInit,
  timeoutMs: number
): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
  
  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });
    clearTimeout(timeoutId);
    return response;
  } catch (error) {
    clearTimeout(timeoutId);
    throw error;
  }
}

// ============ COORDINADOR (desde el origen) ============

export async function transferirProducto(
  producto: string,
  cantidad: number,
  destinoCiudad: string
): Promise<{ success: boolean; message: string; details?: any }> {
  const transactionId = generateTransactionId();
  const origen = config.city;
  const destino = destinoCiudad.toLowerCase();
  
  console.log(`\n🔷 [${transactionId}] Iniciando transferencia: ${origen} → ${destino}`);
  console.log(`   Producto: ${producto}, Cantidad: ${cantidad}`);
  
  const client = await pool.connect();
  
  try {
    // ============ FASE 1: PREPARE ============
    console.log(`📦 [${transactionId}] Fase 1: PREPARE`);
    
    // 1.1 Prepare en origen (descontar stock)
    console.log(`   📍 Prepare en origen (${origen})...`);
    const origenPrepare = await queries.prepareDescuento(client, producto, cantidad);
    
    if (!origenPrepare.success) {
      await client.query('ROLLBACK');
      client.release();
      return {
        success: false,
        message: `Stock insuficiente en ${origen}. Disponible: ${origenPrepare.currentStock}, solicitado: ${cantidad}`
      };
    }
    console.log(`   ✅ Origen OK (stock reservado)`);
    
    // 1.2 Prepare en destino (vía HTTP con fetch)
    console.log(`   📍 Prepare en destino (${destino})...`);
    const destinoUrl = getPeerUrl(destino);
    const preparePayload: PrepareRequest = {
      producto,
      cantidad,
      origen,
      destino,
      transactionId
    };
    
    let destinoPrepare: PrepareResponse;
    try {
      const response = await fetchWithTimeout(
        `${destinoUrl}/api/2pc/prepare`,
        {
          method: 'POST',
          body: JSON.stringify(preparePayload),
        },
        config.prepareTimeout
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      destinoPrepare = await response.json();
    } catch (error: any) {
      console.log(`   ❌ Error conectando con destino: ${error.message}`);
      await client.query('ROLLBACK');
      client.release();
      
      // Determinar tipo de error para mensaje más específico
      if (error.name === 'AbortError') {
        return {
          success: false,
          message: `El almacén de ${destino} no respondió en el tiempo esperado (${config.prepareTimeout / 1000}s). Intente nuevamente.`
        };
      }
      
      return {
        success: false,
        message: `No se pudo contactar con el almacén de ${destino}. Servidor no responde.`
      };
    }
    
    if (!destinoPrepare.success) {
      console.log(`   ❌ Destino rechazó: ${destinoPrepare.message}`);
      await client.query('ROLLBACK');
      client.release();
      return {
        success: false,
        message: `El almacén de ${destino} rechazó la operación: ${destinoPrepare.message}`
      };
    }
    console.log(`   ✅ Destino OK (listo para recibir)`);
    
    // ============ FASE 2: COMMIT ============
    console.log(`📦 [${transactionId}] Fase 2: COMMIT`);
    
    // 2.1 Commit en destino
    console.log(`   📍 Commit en destino (${destino})...`);
    try {
      const response = await fetchWithTimeout(
        `${destinoUrl}/api/2pc/commit`,
        {
          method: 'POST',
          body: JSON.stringify({ transactionId, producto, cantidad }),
        },
        config.commitTimeout
      );
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      
      console.log(`   ✅ Commit destino OK`);
    } catch (error: any) {
      console.log(`   ❌ Error en commit destino: ${error.message}`);
      // Rollback en origen
      await queries.rollbackDescuento(client, producto, cantidad);
      await client.query('COMMIT');
      client.release();
      
      if (error.name === 'AbortError') {
        return {
          success: false,
          message: `Timeout confirmando en ${destino}. La operación fue revertida en ${origen}. Intente nuevamente.`
        };
      }
      
      return {
        success: false,
        message: `Error al confirmar en ${destino}. La operación fue revertida en ${origen}.`
      };
    }
    
    // 2.2 Commit en origen (confirmar el descuento)
    await client.query('COMMIT');
    console.log(`   ✅ Commit origen OK`);
    
    client.release();
    
    console.log(`✅ [${transactionId}] Transferencia completada con éxito!`);
    
    return {
      success: true,
      message: `Transferencia exitosa: ${cantidad} unidad(es) de "${producto}" de ${origen} a ${destino}`,
      details: {
        transactionId,
        producto,
        cantidad,
        origen,
        destino
      }
    };
    
  } catch (error: any) {
    console.log(`❌ [${transactionId}] Error inesperado: ${error.message}`);
    await client.query('ROLLBACK');
    client.release();
    return {
      success: false,
      message: `Error interno: ${error.message}`
    };
  }
}

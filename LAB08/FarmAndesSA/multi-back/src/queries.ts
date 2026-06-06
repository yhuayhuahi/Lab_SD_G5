import pool from './pool';

// ============ INVENTARIO ============

export interface InventarioItem {
  id: number;
  producto: string;
  stock: number;
}

export async function getInventario(): Promise<InventarioItem[]> {
  const result = await pool.query('SELECT * FROM inventario ORDER BY id');
  return result.rows;
}

export async function getStock(producto: string): Promise<number | null> {
  const result = await pool.query(
    'SELECT stock FROM inventario WHERE producto = $1',
    [producto]
  );
  return result.rows[0]?.stock ?? null;
}

export async function getAllProducts(): Promise<string[]> {
  const result = await pool.query('SELECT DISTINCT producto FROM inventario');
  return result.rows.map(row => row.producto);
}

// ============ OPERACIONES PARA 2PC ============

// PREPARE: Intentar descontar stock (reserva)
export async function prepareDescuento(
  client: any,
  producto: string,
  cantidad: number
): Promise<{ success: boolean; currentStock: number }> {
  // Verificar stock actual
  const stockResult = await client.query(
    'SELECT stock FROM inventario WHERE producto = $1 FOR UPDATE',
    [producto]
  );
  
  if (stockResult.rows.length === 0) {
    return { success: false, currentStock: 0 };
  }
  
  const currentStock = stockResult.rows[0].stock;
  
  if (currentStock < cantidad) {
    return { success: false, currentStock };
  }
  
  // Reservar stock (descuento temporal)
  await client.query(
    'UPDATE inventario SET stock = stock - $1 WHERE producto = $2',
    [cantidad, producto]
  );
  
  return { success: true, currentStock };
}

// PREPARE: Solo verificar que destino existe (no suma aún)
export async function prepareSuma(
  client: any,
  producto: string,
  cantidad: number
): Promise<boolean> {
  // Verificar que el producto existe, si no, crearlo con stock 0
  const exists = await client.query(
    'SELECT id FROM inventario WHERE producto = $1',
    [producto]
  );
  
  if (exists.rows.length === 0) {
    // Crear producto con stock 0 (luego se sumará en commit)
    await client.query(
      'INSERT INTO inventario (producto, stock) VALUES ($1, 0)',
      [producto]
    );
  }
  
  return true;
}

// COMMIT: Confirmar la suma (solo en destino)
export async function commitSuma(
  client: any,
  producto: string,
  cantidad: number
): Promise<void> {
  await client.query(
    'UPDATE inventario SET stock = stock + $1 WHERE producto = $2',
    [cantidad, producto]
  );
}

// ROLLBACK: Revertir descuento (solo en origen)
export async function rollbackDescuento(
  client: any,
  producto: string,
  cantidad: number
): Promise<void> {
  await client.query(
    'UPDATE inventario SET stock = stock + $1 WHERE producto = $2',
    [cantidad, producto]
  );
}

// ============ OPERACIONES SIMPLES (para pruebas) ============

export async function crearProducto(producto: string, stockInicial: number = 0): Promise<InventarioItem> {
  const result = await pool.query(
    'INSERT INTO inventario (producto, stock) VALUES ($1, $2) ON CONFLICT (producto) DO UPDATE SET stock = inventario.stock + $2 RETURNING *',
    [producto, stockInicial]
  );
  return result.rows[0];
}

export async function actualizarStock(producto: string, stock: number): Promise<InventarioItem> {
  const result = await pool.query(
    'UPDATE inventario SET stock = $1 WHERE producto = $2 RETURNING *',
    [stock, producto]
  );
  return result.rows[0];
}

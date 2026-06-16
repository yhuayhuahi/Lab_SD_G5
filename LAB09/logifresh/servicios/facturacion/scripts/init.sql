-- Crear tabla de facturas
CREATE TABLE IF NOT EXISTS facturas (
    id SERIAL PRIMARY KEY,
    factura_id VARCHAR(50) UNIQUE NOT NULL,
    pedido_id VARCHAR(50) UNIQUE NOT NULL,
    cliente_id VARCHAR(50) NOT NULL,
    cliente_nombre VARCHAR(200),
    cliente_ruc VARCHAR(20),
    cliente_direccion TEXT,
    subtotal DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    igv DECIMAL(10,2) DEFAULT 0,
    total_con_igv DECIMAL(10,2) GENERATED ALWAYS AS (total + COALESCE(igv, 0)) STORED,
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'EMITIDA',
    promocion_aplicada VARCHAR(50)
);

-- Crear tabla de items de factura
CREATE TABLE IF NOT EXISTS items_factura (
    id SERIAL PRIMARY KEY,
    factura_id INTEGER NOT NULL REFERENCES facturas(id) ON DELETE CASCADE,
    producto_id VARCHAR(50) NOT NULL,
    nombre_producto VARCHAR(100) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    descuento_aplicado DECIMAL(10,2) DEFAULT 0,
    subtotal_item DECIMAL(10,2) GENERATED ALWAYS AS 
        ((precio_unitario * cantidad) - descuento_aplicado) STORED
);

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_facturas_pedido_id ON facturas(pedido_id);
CREATE INDEX IF NOT EXISTS idx_facturas_cliente_id ON facturas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha_emision);
CREATE INDEX IF NOT EXISTS idx_items_factura_id ON items_factura(factura_id);

-- Mensaje de confirmacion
DO $$
BEGIN
    RAISE NOTICE 'Base de datos LogiFresh - Facturación inicializada correctamente';
END $$;

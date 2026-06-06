-- Tabla de inventario simple
CREATE TABLE IF NOT EXISTS inventario (
    id SERIAL PRIMARY KEY,
    producto VARCHAR(100) UNIQUE NOT NULL,
    stock INTEGER DEFAULT 0
);

-- Insertar productos de ejemplo
INSERT INTO inventario (producto, stock) VALUES 
    ('Paracetamol', 100),
    ('Ibuprofeno', 50),
    ('Amoxicilina', 75),
    ('Omeprazol', 30)
ON CONFLICT (producto) DO NOTHING;

-- Índice para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_producto ON inventario(producto);

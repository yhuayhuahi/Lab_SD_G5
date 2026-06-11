# Guia para el desarrollo de la base de datos

## Resumen Rápido por Servicio

| Servicio | Tipo de BD | ¿Por qué? |
|:---|:---|:---|
| **Pedidos** | No relacional (MongoDB) | Documentos autónomos, esquema flexible, fácil de escalar |
| **Inventario** | No requiere (Redis) | Datos en memoria, clave-valor simple |
| **Facturación** | Relacional (PostgreSQL) | Integridad referencial, transacciones ACID |
| **Transporte** | No requiere (Redis) | Estado transitorio, colas |
| **Notificaciones** | No requiere (memoria/archivo) | Solo logs para el laboratorio |

---

## 1. Servicio de Pedidos (MongoDB - No relacional)

### Estructura del Documento

```
Colección: pedidos
┌─────────────────────────────────────────────────────────┐
│ {                                                       │
│   "_id": ObjectId("507f1f77bcf86cd799439011"),        │
│   "pedidoId": "PED-20241120-001",      ← índice único  │
│   "clienteId": "CLI-001",                              │
│   "clienteEmail": "super@mercado.com",                 │
│   "fechaCreacion": ISODate("2024-11-20T10:30:00Z"),   │
│   "estado": "CONFIRMADO",                              │
│   "promocionId": "PROMO-2X1",                          │
│   "total": 3.50,                                       │
│   "facturaId": "FAC-1734567890123",                    │
│   "transportistaId": "CAM-42",                         │
│   "items": [                                           │
│     {                                                  │
│       "productoId": "PROD-YOGURT",                     │
│       "nombre": "Yogut Natural",                       │
│       "cantidad": 2,                                   │
│       "precioUnitario": 3.50                          │
│     }                                                  │
│   ],                                                   │
│   "historialEstados": [                               │
│     { "estado": "PENDIENTE", "fecha": ISODate(...) }, │
│     { "estado": "CONFIRMADO", "fecha": ISODate(...) } │
│   ]                                                   │
│ }                                                     │
└─────────────────────────────────────────────────────────┘
```

### Índices recomendados

```javascript
// Índice único para búsquedas rápidas
db.pedidos.createIndex({ "pedidoId": 1 }, { unique: true })

// Índice para consultar por cliente
db.pedidos.createIndex({ "clienteId": 1 })

// Índice para consultar por fecha
db.pedidos.createIndex({ "fechaCreacion": -1 })
```

### Posibles estados del pedido

```
PENDIENTE → CONFIRMADO → ENVIADO → ENTREGADO
    ↓
CANCELADO
```

### Ventajas de MongoDB aquí

- Los items del pedido van **dentro** del documento (no hace falta JOIN)
- El historial de estados es un array anidado
- Esquema flexible: si mañana agregas "dirección de entrega", no hay migración

---

## 2. Servicio de Inventario (Redis - Clave-Valor)

### Estructura en Redis

```
# Stock actual de productos
┌──────────────────────────────────────┐
│ KEY                    VALUE         │
├──────────────────────────────────────┤
│ "stock:PROD-YOGURT"  → "150"         │
│ "stock:PROD-LECHE"   → "45"          │
│ "stock:PROD-QUELLAVES" → "0"         │
│ "stock:PROD-MANTECA" → "23"          │
│ "stock:PROD-POLLO"   → "78"          │
└──────────────────────────────────────┘

# Reservas temporales (opcional, para mayor realismo)
┌──────────────────────────────────────┐
│ KEY                           VALUE  │
├──────────────────────────────────────┤
│ "reserva:PED-001:PROD-YOGURT" → "2"  │
│ "reserva:PED-002:PROD-LECHE"  → "4"  │
└──────────────────────────────────────┘

# Productos con stock bajo (para alertas)
┌──────────────────────────────────────┐
│ KEY                           VALUE  │
├──────────────────────────────────────┤
│ "stock_bajo:PROD-QUELLAVES" → "true" │
└──────────────────────────────────────┘
```

### Comandos Redis útiles

```bash
# Obtener stock
GET stock:PROD-YOGURT

# Descontar stock (operación atómica)
DECRBY stock:PROD-YOGURT 2

# Incrementar stock (devoluciones)
INCRBY stock:PROD-YOGURT 2

# Verificar existencia
EXISTS stock:PROD-YOGURT
```

### Datos iniciales para cargar al inicio

```javascript
// En el código al arrancar el servicio
await redis.set('stock:PROD-YOGURT', 150);
await redis.set('stock:PROD-LECHE', 45);
await redis.set('stock:PROD-QUELLAVES', 0);
await redis.set('stock:PROD-MANTECA', 23);
await redis.set('stock:PROD-POLLO', 78);
```

---

## 3. Servicio de Facturación (PostgreSQL - Relacional)

### Diagrama Entidad-Relación

```
┌─────────────────────────────────┐
│           facturas              │
├─────────────────────────────────┤
│ PK │ id (serial)                │
│    │ factura_id (varchar, único)│
│    │ pedido_id (varchar)        │
│    │ cliente_id (varchar)       │
│    │ subtotal (decimal)         │
│    │ descuento (decimal)        │
│    │ total (decimal)            │
│    │ fecha_emision (timestamp)  │
│    │ estado (varchar)           │
│    │ promocion_aplicada (varchar│
└─────────────────────────────────┘
              │
              │ 1
              │
              │ N
              ▼
┌─────────────────────────────────┐
│         items_factura           │
├─────────────────────────────────┤
│ PK │ id (serial)                │
│ FK │ factura_id → facturas(id)  │
│    │ producto_id (varchar)      │
│    │ nombre_producto (varchar)  │
│    │ cantidad (integer)         │
│    │ precio_unitario (decimal)  │
│    │ descuento_aplicado (decimal│
└─────────────────────────────────┘
```

### Script SQL de creación

```sql
-- Tabla de facturas
CREATE TABLE facturas (
    id SERIAL PRIMARY KEY,
    factura_id VARCHAR(50) UNIQUE NOT NULL,
    pedido_id VARCHAR(50) NOT NULL,
    cliente_id VARCHAR(50) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    descuento DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    fecha_emision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'EMITIDA',
    promocion_aplicada VARCHAR(50)
);

-- Índices para búsquedas rápidas
CREATE INDEX idx_facturas_pedido_id ON facturas(pedido_id);
CREATE INDEX idx_facturas_cliente_id ON facturas(cliente_id);
CREATE INDEX idx_facturas_fecha ON facturas(fecha_emision);

-- Tabla de items de factura
CREATE TABLE items_factura (
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

-- Índice para consultar items por factura
CREATE INDEX idx_items_factura_id ON items_factura(factura_id);
```

### Ejemplo de datos insertados

```sql
-- Factura
INSERT INTO facturas (factura_id, pedido_id, cliente_id, subtotal, descuento, total, promocion_aplicada)
VALUES ('FAC-1734567890123', 'PED-20241120-001', 'CLI-001', 7.00, 3.50, 3.50, 'PROMO-2X1');

-- Items de la factura
INSERT INTO items_factura (factura_id, producto_id, nombre_producto, cantidad, precio_unitario)
VALUES (1, 'PROD-YOGURT', 'Yogurt Natural', 2, 3.50);
```

### Para simular facturas duplicadas

Antes de insertar, verificar:
```sql
SELECT COUNT(*) FROM facturas WHERE pedido_id = :pedidoId
```
Si > 0, rechazar con error 409.

---

## 4. 🚚 Servicio de Transporte (Redis - Colas y Estados)

### Estructura en Redis

```
# Estado actual de envíos
┌────────────────────────────────────────────┐
│ KEY                           VALUE        │
├────────────────────────────────────────────┤
│ "transporte:PED-001" → "ASIGNADO"          │
│ "transporte:PED-002" → "EN_RUTA"           │
│ "transporte:PED-003" → "ENTREGADO"         │
└────────────────────────────────────────────┘

# Cola de pedidos pendientes de asignar (con Bull o lista Redis)
┌────────────────────────────────────────────┐
│ "cola:transporte:pendientes"              │
│ ┌─────────┬─────────┬─────────┐          │
│ │ PED-004 │ PED-005 │ PED-006 │          │
│ └─────────┴─────────┴─────────┘          │
└────────────────────────────────────────────┘

# Camiones disponibles (Set de Redis)
┌────────────────────────────────────────────┐
│ KEY                    MEMBERS             │
├────────────────────────────────────────────┤
│ "camiones:disponibles" → {"CAM-101",       │
│                           "CAM-202",       │
│                           "CAM-303"}       │
└────────────────────────────────────────────┘

# Camiones ocupados
┌────────────────────────────────────────────┐
│ KEY                    MEMBERS             │
├────────────────────────────────────────────┤
│ "camiones:ocupados" → {"CAM-404"}          │
└────────────────────────────────────────────┘
```

### Estados posibles del transporte

```
ASIGNADO → EN_RUTA → ENTREGADO
    ↓
CANCELADO
```

### Comandos Redis útiles

```bash
# Asignar estado
SET transporte:PED-001 ASIGNADO

# Consultar estado
GET transporte:PED-001

# Agregar a cola (lista)
LPUSH cola:transporte:pendientes PED-007

# Sacar de cola
RPOP cola:transporte:pendientes

# Agregar camión disponible
SADD camiones:disponibles CAM-101

# Quitar camión disponible (ocupar)
SREM camiones:disponibles CAM-101
SADD camiones:ocupados CAM-101
```

---

## 5. Servicio de Notificaciones (Memoria)

### Estructura en memoria (array)

```javascript
// En el código, un array global
const notificaciones = [
  {
    "id": "NOT-1734567890123",
    "email": "cliente@supermercado.com",
    "asunto": "Pedido Confirmado",
    "mensaje": "Tu pedido PED-001 ha sido confirmado",
    "fecha": "2024-11-20T10:30:00Z",
    "estado": "ENVIADO"
  }
];
```

### Para persistencia básica

Si quieren guardar logs entre reinicios:
```javascript
// Guardar en archivo JSON
const fs = require('fs');
fs.appendFileSync('notificaciones.log', JSON.stringify(notificacion) + '\n');
```

---

## Resumen 

| Persona | Servicio | BD | Lo que debe instalar localmente |
|:---|:---|:---|:---|
| 1 | Pedidos | MongoDB | Docker con mongo:6 o MongoDB Compass |
| 2 | Inventario | Redis | Docker con redis:7-alpine |
| 3 | Facturación | PostgreSQL | Docker con postgres:15 o pgAdmin |
| 4 | Transporte | Redis | Docker con redis:7-alpine |
| 5 | Notificaciones | Ninguna | Nada extra |



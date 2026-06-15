##  Prueba completa de todos los endpoints

### 1. Health Check (ya no es funcionalll=)

```bash
curl http://localhost:8083/health
```

**Respuesta esperada:**
```json
{"status":"ok","service":"facturacion","timestamp":"2024-..."}
```

---

### 2. POST `/api/facturas/calcular` (Con promoción 2x1)

```bash
curl -X POST http://localhost:8083/api/facturas/calcular \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productoId": "PROD-YOGURT", "cantidad": 4, "precioUnitario": 3.50 },
      { "productoId": "PROD-LECHE", "cantidad": 2, "precioUnitario": 5.20 }
    ],
    "promocionId": "PROMO-2X1"
  }'
```

---

### 3. POST `/api/facturas/calcular` (Sin promoción)

```bash
curl -X POST http://localhost:8083/api/facturas/calcular \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productoId": "PROD-YOGURT", "cantidad": 4, "precioUnitario": 3.50 },
      { "productoId": "PROD-LECHE", "cantidad": 2, "precioUnitario": 5.20 }
    ]
  }'
```

---

### 4. POST `/api/facturas/calcular` (Promoción 10% OFF con compra > 100)

```bash
curl -X POST http://localhost:8083/api/facturas/calcular \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      { "productoId": "PROD-CARNE", "cantidad": 10, "precioUnitario": 15.00 }
    ],
    "promocionId": "PROMO-10OFF"
  }'
```

---

### 5. POST `/api/facturas/generar` (Crear factura válida)

```bash
curl -X POST http://localhost:8083/api/facturas/generar \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "PED-20241120-001",
    "clienteId": "CLI-001",
    "clienteNombre": "Supermercado Los Andes",
    "clienteRuc": "20512345678",
    "clienteDireccion": "Av. Primavera 123, San Isidro",
    "items": [
      {
        "productoId": "PROD-YOGURT",
        "nombre": "Yogurt Natural Laive",
        "cantidad": 4,
        "precioUnitario": 3.50
      },
      {
        "productoId": "PROD-LECHE",
        "nombre": "Leche Gloria Entera",
        "cantidad": 2,
        "precioUnitario": 5.20
      }
    ],
    "promocionId": "PROMO-2X1"
  }'
```

**Guarda el `facturaId` que te devuelva (ej: `FAC-1732127445123`)**
"facturaId": "FAC-1781385843223"
---

### 6. POST `/api/facturas/generar` (Factura duplicada - debe dar error 409)

```bash
curl -X POST http://localhost:8083/api/facturas/generar \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "PED-20241120-001",
    "clienteId": "CLI-001",
    "clienteNombre": "Supermercado Los Andes",
    "clienteRuc": "20512345678",
    "clienteDireccion": "Av. Primavera 123, San Isidro",
    "items": [
      {
        "productoId": "PROD-YOGURT",
        "nombre": "Yogurt Natural Laive",
        "cantidad": 4,
        "precioUnitario": 3.50
      }
    ],
    "promocionId": "PROMO-2X1"
  }'
```

**Debe responder con:**
```json
{"error":"DUPLICATE_INVOICE","mensaje":"Ya existe una factura para el pedido PED-20241120-001",...}
```

---

### 7. GET `/api/facturas/:id` (Obtener factura creada)

**Reemplaza `FAC-1732127445123` con el ID real que te devolvió el paso 5:**
"facturaId": "FAC-1781384993444"

```bash
curl http://localhost:8083/api/facturas/FAC-1781385843223
```

---

### 8. GET `/api/facturas/pedido/:pedidoId`
"pedidoId": "PED-20241120-001"

```bash
curl http://localhost:8083/api/facturas/pedido/PED-20241120-001
```

---

### 9. GET `/api/facturas` (Listar todas las facturas) 4no funcionall

```bash
curl http://localhost:8083/api/facturas
```

**Con paginación:**
```bash
curl "http://localhost:8083/api/facturas?limit=5&offset=0"
```

---

### 10. GET con ID inexistente (404)

```bash
curl http://localhost:8083/api/facturas/FAC-999999
```

**Debe responder:**
```json
{"error":"NOT_FOUND","mensaje":"No existe factura con ID: FAC-999999",...}
```

---

### 11. Validación de error (400) - Faltan campos

```bash
curl -X POST http://localhost:8083/api/facturas/generar \
  -H "Content-Type: application/json" \
  -d '{
    "pedidoId": "PED-TEST"
  }'
```

**Debe responder con error 400 indicando campos faltantes.**

---

## Resumen de pruebas

| # | Endpoint | Estado esperado |
|:---|:---|:---|
| 1 | `GET /health` | 200 |
| 2 | `POST /calcular` (2x1) | 200 |
| 3 | `POST /calcular` (sin promo) | 200 |
| 4 | `POST /calcular` (10% OFF) | 200 |
| 5 | `POST /generar` (válido) | 201 |
| 6 | `POST /generar` (duplicado) | 409 |
| 7 | `GET /facturas/:id` | 200 |
| 8 | `GET /pedido/:pedidoId` | 200 |
| 9 | `GET /facturas` (listar) | 200 |
| 10 | `GET /facturas/:id` (inexistente) | 404 |
| 11 | `POST /generar` (faltan campos) | 400 |

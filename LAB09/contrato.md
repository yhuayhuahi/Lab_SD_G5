# CONTRATO DE MICROSERVICIOS - LogiFresh

## 1. 🛒 Servicio de Pedidos (Orquestador)

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/pedidos` | Crear un nuevo pedido |
| GET | `/api/pedidos/:id` | Consultar estado de un pedido |
| PUT | `/api/pedidos/:id/cancelar` | Cancelar pedido |
| POST | `/api/pedidos/test/lento` | Simular lentitud (>8 seg) |

---

### POST `/api/pedidos`

**Request Body:**
```json
{
  "clienteId": "CLI-001",
  "clienteEmail": "ventas@supermercadoandes.com",
  "clienteNombre": "Supermercado Los Andes",
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
  "promocionId": "PROMO-2X1",
  "notas": "Entregar en horario de 8am a 12pm"
}
```

**Response 201 - Created:**
```json
{
  "pedidoId": "PED-20241120-001",
  "clienteId": "CLI-001",
  "clienteEmail": "ventas@supermercadoandes.com",
  "fechaCreacion": "2024-11-20T10:30:45.123Z",
  "estado": "CONFIRMADO",
  "items": [
    {
      "productoId": "PROD-YOGURT",
      "nombre": "Yogurt Natural Laive",
      "cantidad": 4,
      "precioUnitario": 3.50,
      "subtotal": 14.00
    },
    {
      "productoId": "PROD-LECHE",
      "nombre": "Leche Gloria Entera",
      "cantidad": 2,
      "precioUnitario": 5.20,
      "subtotal": 10.40
    }
  ],
  "subtotal": 24.40,
  "descuento": 5.20,
  "total": 19.20,
  "facturaId": "FAC-1732127445123",
  "transportistaId": "CAM-42",
  "tiempoEstimadoEntregaMin": 45
}
```

**Response 400 - Bad Request:**
```json
{
  "error": "VALIDATION_ERROR",
  "mensaje": "El campo clienteId es requerido",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

**Response 409 - Conflict (Stock insuficiente):**
```json
{
  "error": "INSUFFICIENT_STOCK",
  "mensaje": "No hay suficiente stock para completar el pedido",
  "detalles": [
    {
      "productoId": "PROD-QUELLAVES",
      "nombre": "Quellaves Premium",
      "solicitado": 6,
      "stockActual": 0,
      "faltante": 6
    }
  ],
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

**Response 409 - Conflict (Factura duplicada):**
```json
{
  "error": "DUPLICATE_INVOICE",
  "mensaje": "Ya existe una factura para este pedido",
  "facturaId": "FAC-1732127445123",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

**Response 500 - Internal Error:**
```json
{
  "error": "SERVICE_UNAVAILABLE",
  "mensaje": "El servicio de inventario no responde. Intente nuevamente",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### GET `/api/pedidos/:id`

**Path Parameter:** `id` = `PED-20241120-001`

**Response 200 - OK:**
```json
{
  "pedidoId": "PED-20241120-001",
  "clienteId": "CLI-001",
  "clienteEmail": "ventas@supermercadoandes.com",
  "clienteNombre": "Supermercado Los Andes",
  "fechaCreacion": "2024-11-20T10:30:45.123Z",
  "fechaActualizacion": "2024-11-20T10:35:12.456Z",
  "estado": "CONFIRMADO",
  "items": [
    {
      "productoId": "PROD-YOGURT",
      "nombre": "Yogurt Natural Laive",
      "cantidad": 4,
      "precioUnitario": 3.50
    }
  ],
  "total": 19.20,
  "facturaId": "FAC-1732127445123",
  "transportistaId": "CAM-42",
  "estadoTransporte": "EN_RUTA",
  "historialEstados": [
    { "estado": "PENDIENTE", "fecha": "2024-11-20T10:30:45.123Z" },
    { "estado": "CONFIRMADO", "fecha": "2024-11-20T10:30:47.456Z" }
  ]
}
```

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No existe un pedido con ID: PED-20241120-999",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### PUT `/api/pedidos/:id/cancelar`

**Path Parameter:** `id` = `PED-20241120-001`

**Request Body (opcional):**
```json
{
  "motivo": "Cliente solicitó cancelación por cambio de pedido"
}
```

**Response 200 - OK:**
```json
{
  "pedidoId": "PED-20241120-001",
  "estadoAnterior": "CONFIRMADO",
  "estadoActual": "CANCELADO",
  "fechaCancelacion": "2024-11-20T11:45:00.000Z",
  "motivo": "Cliente solicitó cancelación por cambio de pedido",
  "recursosLiberados": {
    "stock": true,
    "factura": true,
    "transporte": true
  }
}
```

**Response 400 - Bad Request (Pedido ya cancelado):**
```json
{
  "error": "INVALID_STATE",
  "mensaje": "El pedido PED-20241120-001 ya se encuentra CANCELADO",
  "estadoActual": "CANCELADO",
  "timestamp": "2024-11-20T11:45:00.000Z"
}
```

**Response 400 - Bad Request (Pedido ya entregado):**
```json
{
  "error": "INVALID_STATE",
  "mensaje": "No se puede cancelar un pedido ya ENTREGADO",
  "estadoActual": "ENTREGADO",
  "timestamp": "2024-11-20T11:45:00.000Z"
}
```

---

### POST `/api/pedidos/test/lento`

**Request Body:** (mismo que POST `/api/pedidos`)

**Response:** (mismo que POST `/api/pedidos` pero con 9 segundos de delay)

---

## 2. 📦 Servicio de Inventario

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/inventario/validar` | Verificar stock |
| POST | `/api/inventario/reservar` | Reservar stock |
| POST | `/api/inventario/liberar` | Liberar stock |
| GET | `/api/inventario/stock/:productoId` | Consultar stock |
| PUT | `/api/inventario/stock/:productoId` | Actualizar stock |

---

### POST `/api/inventario/validar`

**Request Body:**
```json
{
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

**Response 200 - OK (Stock suficiente):**
```json
{
  "disponible": true,
  "detalles": [
    {
      "productoId": "PROD-YOGURT",
      "disponible": true,
      "stockActual": 150,
      "solicitado": 4
    },
    {
      "productoId": "PROD-LECHE",
      "disponible": true,
      "stockActual": 45,
      "solicitado": 2
    }
  ],
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

**Response 200 - OK (Stock insuficiente):**
```json
{
  "disponible": false,
  "detalles": [
    {
      "productoId": "PROD-YOGURT",
      "disponible": true,
      "stockActual": 150,
      "solicitado": 4
    },
    {
      "productoId": "PROD-QUELLAVES",
      "disponible": false,
      "stockActual": 0,
      "solicitado": 6,
      "faltante": 6
    }
  ],
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

**Response 400 - Bad Request:**
```json
{
  "error": "VALIDATION_ERROR",
  "mensaje": "El campo items es requerido y debe ser un array no vacío",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### POST `/api/inventario/reservar`

**Request Body:**
```json
{
  "pedidoId": "PED-20241120-001",
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

**Response 200 - OK:**
```json
{
  "success": true,
  "mensaje": "Stock reservado exitosamente",
  "reservas": [
    { "productoId": "PROD-YOGURT", "cantidadReservada": 4, "stockRestante": 146 },
    { "productoId": "PROD-LECHE", "cantidadReservada": 2, "stockRestante": 43 }
  ],
  "timestamp": "2024-11-20T10:30:47.456Z"
}
```

**Response 409 - Conflict (Stock insuficiente al reservar):**
```json
{
  "error": "RESERVATION_FAILED",
  "mensaje": "No se pudo reservar el stock. Stock insuficiente",
  "detalles": [
    {
      "productoId": "PROD-LECHE",
      "disponible": 43,
      "solicitado": 50,
      "faltante": 7
    }
  ],
  "timestamp": "2024-11-20T10:30:47.456Z"
}
```

---

### POST `/api/inventario/liberar`

**Request Body:**
```json
{
  "pedidoId": "PED-20241120-001",
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

**Response 200 - OK:**
```json
{
  "success": true,
  "mensaje": "Stock liberado exitosamente",
  "liberaciones": [
    { "productoId": "PROD-YOGURT", "cantidadLiberada": 4, "stockActual": 150 },
    { "productoId": "PROD-LECHE", "cantidadLiberada": 2, "stockActual": 45 }
  ],
  "timestamp": "2024-11-20T11:45:05.000Z"
}
```

---

### GET `/api/inventario/stock/:productoId`

**Path Parameter:** `productoId` = `PROD-YOGURT`

**Response 200 - OK:**
```json
{
  "productoId": "PROD-YOGURT",
  "stockActual": 146,
  "stockReservado": 4,
  "stockDisponible": 142,
  "ultimaActualizacion": "2024-11-20T10:30:47.456Z"
}
```

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No existe producto con ID: PROD-INEXISTENTE",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### PUT `/api/inventario/stock/:productoId`

**Path Parameter:** `productoId` = `PROD-YOGURT`

**Request Body:**
```json
{
  "operacion": "INCREMENTAR",  // o "DECREMENTAR" o "SET"
  "cantidad": 100,
  "motivo": "Reabastecimiento de inventario"
}
```

**Response 200 - OK:**
```json
{
  "productoId": "PROD-YOGURT",
  "operacion": "INCREMENTAR",
  "valorAnterior": 146,
  "valorNuevo": 246,
  "timestamp": "2024-11-20T14:00:00.000Z"
}
```

---

## 3. 🧾 Servicio de Facturación

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/facturas/calcular` | Calcular totales (sin guardar) |
| POST | `/api/facturas/generar` | Generar y guardar factura |
| GET | `/api/facturas/:id` | Obtener factura por ID |
| GET | `/api/facturas/pedido/:pedidoId` | Obtener factura por pedido |

---

### POST `/api/facturas/calcular`

**Request Body:**
```json
{
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4, "precioUnitario": 3.50 },
    { "productoId": "PROD-LECHE", "cantidad": 2, "precioUnitario": 5.20 }
  ],
  "promocionId": "PROMO-2X1"
}
```

**Response 200 - OK:**
```json
{
  "subtotal": 24.40,
  "descuento": 5.20,
  "total": 19.20,
  "detalleDescuento": "Promoción 2x1 aplicada en producto más barato (Leche)",
  "promocionAplicada": "PROMO-2X1",
  "timestamp": "2024-11-20T10:30:46.789Z"
}
```

**Response 200 - OK (Sin promoción):**
```json
{
  "subtotal": 24.40,
  "descuento": 0,
  "total": 24.40,
  "detalleDescuento": "No se aplicaron promociones",
  "promocionAplicada": null,
  "timestamp": "2024-11-20T10:30:46.789Z"
}
```

---

### POST `/api/facturas/generar`

**Request Body:**
```json
{
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
      "precioUnitario": 3.50,
      "descuentoAplicado": 0
    },
    {
      "productoId": "PROD-LECHE",
      "nombre": "Leche Gloria Entera",
      "cantidad": 2,
      "precioUnitario": 5.20,
      "descuentoAplicado": 5.20
    }
  ],
  "subtotal": 24.40,
  "descuentoTotal": 5.20,
  "total": 19.20,
  "promocionAplicada": "PROMO-2X1"
}
```

**Response 201 - Created:**
```json
{
  "facturaId": "FAC-1732127445123",
  "pedidoId": "PED-20241120-001",
  "clienteId": "CLI-001",
  "clienteNombre": "Supermercado Los Andes",
  "clienteRuc": "20512345678",
  "fechaEmision": "2024-11-20T10:30:47.123Z",
  "subtotal": 24.40,
  "descuentoTotal": 5.20,
  "total": 19.20,
  "igv": 3.46,
  "totalConIgv": 22.66,
  "promocionAplicada": "PROMO-2X1",
  "estado": "EMITIDA",
  "urlPdf": "/facturas/FAC-1732127445123.pdf"
}
```

**Response 409 - Conflict (Factura duplicada):**
```json
{
  "error": "DUPLICATE_INVOICE",
  "mensaje": "Ya existe una factura para el pedido PED-20241120-001",
  "facturaExistente": "FAC-1732127445123",
  "timestamp": "2024-11-20T10:30:47.123Z"
}
```

---

### GET `/api/facturas/:id`

**Path Parameter:** `id` = `FAC-1732127445123`

**Response 200 - OK:**
```json
{
  "facturaId": "FAC-1732127445123",
  "pedidoId": "PED-20241120-001",
  "clienteId": "CLI-001",
  "clienteNombre": "Supermercado Los Andes",
  "clienteRuc": "20512345678",
  "clienteDireccion": "Av. Primavera 123, San Isidro",
  "fechaEmision": "2024-11-20T10:30:47.123Z",
  "items": [
    {
      "productoId": "PROD-YOGURT",
      "nombre": "Yogurt Natural Laive",
      "cantidad": 4,
      "precioUnitario": 3.50,
      "descuentoAplicado": 0,
      "subtotal": 14.00
    },
    {
      "productoId": "PROD-LECHE",
      "nombre": "Leche Gloria Entera",
      "cantidad": 2,
      "precioUnitario": 5.20,
      "descuentoAplicado": 5.20,
      "subtotal": 5.20
    }
  ],
  "subtotal": 24.40,
  "descuentoTotal": 5.20,
  "total": 19.20,
  "igv": 3.46,
  "totalConIgv": 22.66,
  "promocionAplicada": "PROMO-2X1",
  "estado": "EMITIDA"
}
```

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No existe factura con ID: FAC-999",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### GET `/api/facturas/pedido/:pedidoId`

**Path Parameter:** `pedidoId` = `PED-20241120-001`

**Response 200 - OK:** (mismo formato que GET `/api/facturas/:id`)

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No existe factura para el pedido: PED-999",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

## 4. 🚚 Servicio de Transporte

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/transporte/asignar` | Asignar camión |
| GET | `/api/transporte/estado/:pedidoId` | Consultar estado |
| PUT | `/api/transporte/estado/:pedidoId` | Actualizar estado |
| GET | `/api/transporte/camiones` | Listar camiones |

---

### POST `/api/transporte/asignar`

**Request Body:**
```json
{
  "pedidoId": "PED-20241120-001",
  "clienteDireccion": "Av. Primavera 123, San Isidro",
  "tipoProducto": "REFRIGERADO",
  "prioridad": "ALTA"
}
```

**Response 200 - OK:**
```json
{
  "transportistaId": "CAM-42",
  "pedidoId": "PED-20241120-001",
  "estado": "ASIGNADO",
  "tiempoEstimadoMin": 45,
  "distanciaKm": 12.5,
  "fechaAsignacion": "2024-11-20T10:30:48.000Z",
  "conductor": {
    "nombre": "Carlos Mendoza",
    "telefono": "+51987654321"
  },
  "placa": "ABI-789"
}
```

**Response 400 - Bad Request (Sin camiones disponibles):**
```json
{
  "error": "NO_TRUCKS_AVAILABLE",
  "mensaje": "No hay camiones disponibles en este momento",
  "tiempoEstimadoEsperaMin": 15,
  "timestamp": "2024-11-20T10:30:48.000Z"
}
```

---

### GET `/api/transporte/estado/:pedidoId`

**Path Parameter:** `pedidoId` = `PED-20241120-001`

**Response 200 - OK:**
```json
{
  "pedidoId": "PED-20241120-001",
  "transportistaId": "CAM-42",
  "estado": "EN_RUTA",
  "ubicacionActual": {
    "lat": -12.0433,
    "lng": -77.0282,
    "direccion": "Av. Javier Prado Este 1234, San Isidro"
  },
  "actualizacion": "2024-11-20T11:15:00.000Z",
  "tiempoEstimadoRestanteMin": 25,
  "conductor": {
    "nombre": "Carlos Mendoza",
    "telefono": "+51987654321"
  }
}
```

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No hay registro de transporte para el pedido: PED-999",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### PUT `/api/transporte/estado/:pedidoId`

**Path Parameter:** `pedidoId` = `PED-20241120-001`

**Request Body:**
```json
{
  "estado": "ENTREGADO",
  "observaciones": "Entregado al almacén. Recibido por Juan Pérez",
  "fotoEvidencia": "https://logifresh.s3.amazonaws.com/evidencias/PED-001.jpg"
}
```

**Response 200 - OK:**
```json
{
  "pedidoId": "PED-20241120-001",
  "transportistaId": "CAM-42",
  "estadoAnterior": "EN_RUTA",
  "estadoActual": "ENTREGADO",
  "fechaActualizacion": "2024-11-20T11:45:00.000Z",
  "tiempoTotalMin": 75
}
```

**Response 400 - Bad Request (Transición inválida):**
```json
{
  "error": "INVALID_STATE_TRANSITION",
  "mensaje": "No se puede cambiar de estado ASIGNADO a ENTREGADO. Debe pasar por EN_RUTA",
  "estadoActual": "ASIGNADO",
  "estadosPermitidos": ["EN_RUTA", "CANCELADO"],
  "timestamp": "2024-11-20T11:45:00.000Z"
}
```

---

### GET `/api/transporte/camiones`

**Query Parameters (opcionales):**
- `estado` = `DISPONIBLE` | `OCUPADO` | `MANTENIMIENTO`
- `tipo` = `REFRIGERADO` | `SECO`

**Response 200 - OK:**
```json
{
  "total": 8,
  "disponibles": 3,
  "camiones": [
    {
      "transportistaId": "CAM-42",
      "placa": "ABI-789",
      "tipo": "REFRIGERADO",
      "capacidadKg": 2500,
      "estado": "DISPONIBLE",
      "conductor": "Carlos Mendoza",
      "ubicacion": "Callao - Terminal Logístico"
    },
    {
      "transportistaId": "CAM-101",
      "placa": "ABC-123",
      "tipo": "REFRIGERADO",
      "capacidadKg": 3500,
      "estado": "OCUPADO",
      "conductor": "María Torres",
      "pedidoActual": "PED-20241119-045",
      "ubicacion": "San Miguel - En ruta"
    }
  ],
  "timestamp": "2024-11-20T10:30:48.000Z"
}
```

---

## 5. 📧 Servicio de Notificaciones

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/notificaciones/enviar` | Enviar correo |
| GET | `/api/notificaciones/estado/:id` | Consultar estado |
| GET | `/api/notificaciones/historial` | Listar notificaciones |
| POST | `/api/notificaciones/test/fallo` | Simular fallo (para pruebas) |
| POST | `/api/notificaciones/test/lento` | Simular retraso (para pruebas) |

---

### POST `/api/notificaciones/enviar`

**Request Body:**
```json
{
  "email": "ventas@supermercadoandes.com",
  "asunto": "Confirmación de Pedido - LogiFresh",
  "mensaje": "Estimado cliente, su pedido PED-20241120-001 ha sido confirmado. Total: S/19.20. Tiempo estimado de entrega: 45 minutos.",
  "pedidoId": "PED-20241120-001",
  "template": "PEDIDO_CONFIRMADO",
  "datosAdicionales": {
    "total": 19.20,
    "transportistaId": "CAM-42",
    "tiempoEstimadoMin": 45
  }
}
```

**Response 200 - OK:**
```json
{
  "success": true,
  "notificacionId": "NOT-1732127448123",
  "email": "ventas@supermercadoandes.com",
  "estado": "ENVIADO",
  "fechaEnvio": "2024-11-20T10:30:49.000Z",
  "mensaje": "Correo enviado exitosamente (simulado)"
}
```

**Response 500 - Internal Error (Fallo aleatorio para pruebas):**
```json
{
  "error": "EMAIL_SERVICE_FAILED",
  "mensaje": "No se pudo enviar el correo. El servicio de email no responde",
  "codigoError": "SMTP_CONNECTION_REFUSED",
  "notificacionId": "NOT-1732127448123",
  "timestamp": "2024-11-20T10:30:49.000Z"
}
```

---

### GET `/api/notificaciones/estado/:id`

**Path Parameter:** `id` = `NOT-1732127448123`

**Response 200 - OK:**
```json
{
  "notificacionId": "NOT-1732127448123",
  "email": "ventas@supermercadoandes.com",
  "asunto": "Confirmación de Pedido - LogiFresh",
  "estado": "ENVIADO",
  "fechaEnvio": "2024-11-20T10:30:49.000Z",
  "intentos": 1,
  "ultimoError": null
}
```

**Response 404 - Not Found:**
```json
{
  "error": "NOT_FOUND",
  "mensaje": "No existe notificación con ID: NOT-999",
  "timestamp": "2024-11-20T10:30:45.123Z"
}
```

---

### GET `/api/notificaciones/historial`

**Query Parameters (opcionales):**
- `email` = `ventas@supermercadoandes.com`
- `estado` = `ENVIADO` | `FALLIDO` | `PENDIENTE`
- `limit` = `10` (default)
- `offset` = `0`

**Response 200 - OK:**
```json
{
  "total": 25,
  "limit": 10,
  "offset": 0,
  "notificaciones": [
    {
      "notificacionId": "NOT-1732127448123",
      "email": "ventas@supermercadoandes.com",
      "asunto": "Confirmación de Pedido - LogiFresh",
      "estado": "ENVIADO",
      "fechaEnvio": "2024-11-20T10:30:49.000Z"
    },
    {
      "notificacionId": "NOT-1732127448000",
      "email": "cliente@otro.com",
      "asunto": "Pedido Cancelado",
      "estado": "FALLIDO",
      "fechaEnvio": "2024-11-20T09:15:00.000Z",
      "error": "Email inválido"
    }
  ]
}
```

---

### POST `/api/notificaciones/test/fallo`

**Request Body:** (mismo que POST `/api/notificaciones/enviar`)

**Response 500 - Internal Error:**
```json
{
  "error": "SIMULATED_FAILURE",
  "mensaje": "Fallo simulado para pruebas de integración",
  "timestamp": "2024-11-20T10:30:49.000Z"
}
```

---

### POST `/api/notificaciones/test/lento`

**Request Body:** (mismo que POST `/api/notificaciones/enviar`)

**Response:** (mismo que POST `/api/notificaciones/enviar` pero con 5 segundos de delay)

---

# 📊 Resumen de Códigos HTTP

| Código | Significado | Uso común |
|:---|:---|:---|
| 200 | OK | GET, PUT exitosos |
| 201 | Created | POST exitoso que crea recurso |
| 400 | Bad Request | Validación fallida |
| 404 | Not Found | Recurso no existe |
| 409 | Conflict | Stock insuficiente, factura duplicada |
| 500 | Internal Error | Servicio externo falla, timeout |

---

# 🔗 URLs en Integración Docker

Cuando estén en el `docker-compose.yml` global, las URLs serán:

```
http://pedidos:8081/api/...
http://inventario:8082/api/...
http://facturacion:8083/api/...
http://transporte:8084/api/...
http://notificaciones:8085/api/...
```

**En desarrollo local (cada uno solo):**
```
http://localhost:8081/api/...
http://localhost:8082/api/...
http://localhost:8083/api/...
http://localhost:8084/api/...
http://localhost:8085/api/...
```


## Visión General de la Estrategia

```
Fase 1 (Desarrollo individual)          Fase 2 (Integración)
─────────────────────────────          ─────────────────────
Cada persona:                           Equipo completo:
1. Carpeta propia                       1. docker-compose.yml global
2. Su propio Dockerfile                  2. Referencia a imágenes locales
3. Su propio docker-compose.yml      3. Red compartida entre servicios
4. Servicio funcionando solo             4. Todo corriendo junto
```

---

## 🔧 Configuración Base (para copiar en cada servicio)

Cada servicio debe partir de esta estructura mínima que ya conoces:

```
mi-servicio/
├── Dockerfile
├── docker-compose.yml
├── package.json
├── tsconfig.json
├── .env.example
├── .dockerignore
└── src/
    └── index.ts
```

**Conceptos de configuración base:**
- `express` para el servidor HTTP
- `dotenv` para variables de entorno
- `tsx` para ejecutar TypeScript en desarrollo
- Puerto definido por variable de entorno (no hardcodeado)

---

## Guías por Microservicio

### 1. Servicio de Pedidos (Orquestador)

**Responsabilidad principal:** 
Recibir solicitudes de pedido, orquestar la llamada a los otros 4 servicios, y consolidar la respuesta.

**Endpoints:**

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/pedidos` | Crear un nuevo pedido (flujo completo) |
| GET | `/api/pedidos/:id` | Consultar estado de un pedido |
| PUT | `/api/pedidos/:id/cancelar` | Cancelar pedido y liberar recursos |
| POST | `/api/pedidos/test/lento` | **Para pruebas:** endpoint que demora 9 segundos |

**Flujo principal (POST /api/pedidos):**
1. Recibir datos del pedido (cliente, items, promoción)
2. Llamar a Inventario (`POST /api/inventario/validar`) para verificar stock
3. Si hay stock, llamar a Facturación (`POST /api/facturas/calcular`) para obtener total con promociones
4. Llamar a Facturación (`POST /api/facturas/generar`) para crear factura
5. Llamar a Transporte (`POST /api/transporte/asignar`) para asignar camión
6. Llamar a Notificaciones (`POST /api/notificaciones/enviar`) para avisar al cliente (fire-and-forget)
7. Guardar pedido en su propia BD (MongoDB)
8. Responder con el pedido confirmado

**Tecnologías adicionales:**
- **MongoDB** para persistencia propia
- **Axios** o `fetch` - para llamar a otros servicios

**Variables de entorno necesarias:**
```
PORT=8081
INVENTARIO_URL=http://localhost:8082
FACTURACION_URL=http://localhost:8083
TRANSPORTE_URL=http://localhost:8084
NOTIFICACIONES_URL=http://localhost:8085
MONGO_URI=mongodb://localhost:27017/pedidos
```

**Para pruebas de integración (Actividad 3):**
- Implementar **Timeout** en llamadas HTTP (ej. 2 segundos máx)
- Implementar **Retry** (reintentar 2-3 veces ante fallos)
- Simular **Circuit Breaker** básico (si falla 3 veces, no llamar por 10 seg)

---

### 2. 📦 Servicio de Inventario

**Responsabilidad principal:**
Gestionar el stock de productos refrigerados y validar disponibilidad.

**Endpoints:**

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/inventario/validar` | Verificar si hay stock suficiente |
| POST | `/api/inventario/reservar` | Reservar stock (para pedidos confirmados) |
| POST | `/api/inventario/liberar` | Liberar stock (para cancelaciones) |
| GET | `/api/inventario/stock/:productoId` | Consultar stock actual |
| PUT | `/api/inventario/stock/:productoId` | Actualizar stock manualmente |

**Payload de ejemplo (/validar):**
```json
{
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 2 }
  ]
}
```

**Respuesta de ejemplo:**
```json
{
  "disponible": true,
  "detalles": [
    { "productoId": "PROD-YOGURT", "disponible": true, "stockActual": 150 }
  ]
}
```

**Tecnologías adicionales:**
- **Redis** (recomendado) o cualquier base de datos en memoria
- Stock inicial pre-cargado al iniciar

**Variables de entorno:**
```
PORT=8082
REDIS_HOST=localhost
REDIS_PORT=6379
```

**Datos iniciales sugeridos (para pruebas):**
- Yogurt: 150 unidades
- Leche: 45 unidades
- Quellaves (cerveza): 0 unidades (para probar stock insuficiente)

**Para pruebas (Actividad 2):**
- Tener al menos un producto con stock = 0 para probar "inventario insuficiente"

---

### 3. Servicio de Facturación

**Responsabilidad principal:**
Calcular totales con promociones, generar facturas únicas y persistirlas.

**Endpoints:**

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/facturas/calcular` | Calcular subtotal, descuento y total (sin guardar) |
| POST | `/api/facturas/generar` | Generar y guardar factura |
| GET | `/api/facturas/:id` | Obtener factura por ID |
| GET | `/api/facturas/pedido/:pedidoId` | Obtener factura por pedido |

**Reglas de promoción a implementar:**

| Promoción ID | Regla |
|:---|:---|
| `PROMO-2X1` | La unidad de menor valor es gratis (por cada 2 unidades, pagar 1) |
| `PROMO-10OFF` | 10% de descuento si el subtotal > S/100 |
| `NINGUNA` o null | Sin descuento |

**Payload de ejemplo (/calcular):**
```json
{
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 2, "precioUnitario": 3.50 }
  ],
  "promocionId": "PROMO-2X1"
}
```

**Respuesta de ejemplo:**
```json
{
  "subtotal": 7.00,
  "descuento": 3.50,
  "total": 3.50
}
```

**Tecnologías adicionales:**
- **PostgreSQL** (recomendado) con TypeORM o Prisma
- Tablas: `facturas` y `items_factura`

**Variables de entorno:**
```
PORT=8083
DB_HOST=localhost
DB_PORT=5432
DB_USER=admin
DB_PASSWORD=secret
DB_NAME=logifresh
```

**Para simular el problema de "facturas duplicadas" (Actividad 2):**
- Implementar un control que rechace generar factura si ya existe una para el mismo `pedidoId`
- Devolver error HTTP 409 en ese caso

---

### 4. Servicio de Transporte

**Responsabilidad principal:**
Asignar camiones refrigerados a los pedidos y gestionar el estado del envío.

**Endpoints:**

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/transporte/asignar` | Asignar un camión a un pedido |
| GET | `/api/transporte/estado/:pedidoId` | Consultar estado de envío |
| PUT | `/api/transporte/estado/:pedidoId` | Actualizar estado (entregado, en ruta, etc.) |
| GET | `/api/transporte/camiones` | Listar camiones disponibles |

**Payload de ejemplo (/asignar):**
```json
{
  "pedidoId": "PED-001"
}
```

**Respuesta de ejemplo:**
```json
{
  "transportistaId": "CAM-42",
  "estado": "ASIGNADO",
  "tiempoEstimadoMin": 45
}
```

**Tecnologías adicionales:**
- **Redis** o simplemente un array en memoria
- Se puede usar **Bull** (cola en Redis) si quieren simular procesamiento asíncrono

**Variables de entorno:**
```
PORT=8084
REDIS_HOST=localhost
REDIS_PORT=6379
```

**Lista de camiones sugerida (fija):**
- CAM-101
- CAM-202
- CAM-303
- CAM-404 (asignar en round-robin)

---

### 5. Servicio de Notificaciones

**Responsabilidad principal:**
Enviar correos electrónicos a los clientes (simulado, solo logs para el laboratorio).

**Endpoints:**

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/notificaciones/enviar` | Enviar correo |
| GET | `/api/notificaciones/estado/:id` | Consultar estado del envío |
| GET | `/api/notificaciones/historial` | Listar notificaciones enviadas (útil para pruebas) |

**Payload de ejemplo (/enviar):**
```json
{
  "email": "cliente@supermercado.com",
  "asunto": "Pedido Confirmado",
  "mensaje": "Tu pedido PED-001 ha sido confirmado"
}
```

**Respuesta de ejemplo:**
```json
{
  "success": true,
  "notificacionId": "NOT-1734567890123",
  "mensaje": "Correo enviado (real SOLO SI NO ES POSIBLE -> simulado :v)"
}
```

**Tecnologías adicionales:**
- **Ninguna base de datos** (solo array en memoria para historial)
- **console.log** para simular envío (se verá en los logs del contenedor)

**Variables de entorno:**
```
PORT=8085
```

**Para pruebas (Actividad 3 - fallos):**
- Implementar **fallo aleatorio** del 10-20% (simular que el servicio de correo a veces falla)
- Cuando falla, responder con HTTP 500

**Para simular "retrasos en confirmaciones" (problema del caso):**
- Añadir un endpoint `/api/notificaciones/enviar/lento` que demore 5 segundos

---

**Nota importante:** La red se crea automáticamente, por lo que los servicios se ven entre sí usando su nombre de servicio (ej. `http://inventario:8082`).

---

## 📝 Checklist para cada miembro

Para asegurar que luego todo integre correctamente:

- [ ] Usar variables de entorno para las URLs de otros servicios (NO hardcodear `localhost`)
- [ ] Exponer puerto via `PORT` desde `.env`
- [ ] Responder con JSON consistente
- [ ] Usar códigos HTTP adecuados (200, 201, 400, 404, 409, 500)
- [ ] Implementar al menos **un problema simulado** (lentitud, duplicados, fallo aleatorio)
- [ ] Tener un `Dockerfile` que exponga el puerto y ejecute (`npm run dev`)
- [ ] Tener un `docker-compose.dev.yml` propio para pruebas individuales


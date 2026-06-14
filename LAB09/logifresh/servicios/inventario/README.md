# 📦 Servicio de Inventario — LogiFresh S.A.

Microservicio encargado de gestionar el stock de productos refrigerados.  
Tecnología: **Node.js + Express + Redis 7**  
Puerto: **3002**

---

## Requisitos

- Docker Desktop instalado y corriendo
- (Opcional para desarrollo local) Node.js 18+

---

## Levantar el servicio

```bash
# Desde la carpeta inventario/
docker-compose up --build
```

Para verificar que está corriendo:
```
GET http://localhost:3002/health
```

---

## Endpoints

| Método | Endpoint | Descripción |
|:---|:---|:---|
| POST | `/api/inventario/validar` | Verificar stock sin modificarlo |
| POST | `/api/inventario/reservar` | Reservar stock para un pedido |
| POST | `/api/inventario/liberar` | Liberar stock al cancelar un pedido |
| GET | `/api/inventario/stock/:productoId` | Consultar stock de un producto |
| PUT | `/api/inventario/stock/:productoId` | Actualizar stock manualmente |

---

## Productos precargados al iniciar

| Producto ID | Nombre | Stock inicial |
|:---|:---|:---:|
| PROD-YOGURT | Yogurt Natural Laive | 150 |
| PROD-LECHE | Leche Gloria Entera | 45 |
| PROD-QUELLAVES | Quellaves Premium | 0 |
| PROD-MANTECA | Manteca | 23 |
| PROD-POLLO | Pollo | 78 |

---

## Ejemplos de uso (Postman / Insomnia)

### POST /api/inventario/validar
```json
POST http://localhost:3002/api/inventario/validar
Content-Type: application/json

{
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

### POST /api/inventario/reservar
```json
POST http://localhost:3002/api/inventario/reservar
Content-Type: application/json

{
  "pedidoId": "PED-20241120-001",
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

### POST /api/inventario/liberar
```json
POST http://localhost:3002/api/inventario/liberar
Content-Type: application/json

{
  "pedidoId": "PED-20241120-001",
  "items": [
    { "productoId": "PROD-YOGURT", "cantidad": 4 },
    { "productoId": "PROD-LECHE", "cantidad": 2 }
  ]
}
```

### GET /api/inventario/stock/:productoId
```
GET http://localhost:3002/api/inventario/stock/PROD-YOGURT
```

### PUT /api/inventario/stock/:productoId
```json
PUT http://localhost:3002/api/inventario/stock/PROD-YOGURT
Content-Type: application/json

{
  "operacion": "INCREMENTAR",
  "cantidad": 100,
  "motivo": "Reabastecimiento de inventario"
}
```
Operaciones válidas: `INCREMENTAR`, `DECREMENTAR`, `SET`

---

## Estructura del proyecto

```
inventario/
├── src/
│   ├── config/
│   │   └── redis.js           # Conexión al cliente Redis
│   ├── controllers/
│   │   └── inventarioController.js  # Handlers HTTP
│   ├── middlewares/
│   │   └── errorHandler.js    # Manejo de errores global
│   ├── routes/
│   │   └── inventario.js      # Definición de rutas
│   ├── services/
│   │   └── inventarioService.js     # Lógica de negocio + Redis
│   └── index.js               # Punto de entrada
├── .env.example
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── package.json
└── README.md
```
**Correcto.** Aquí tienes la estructura de archivos definitiva, diseñada para que **cada miembro trabaje en su carpeta de forma independiente** y luego todo se integre limpiamente.

---

## 📁 Estructura Final del Proyecto

```
indicaciones.md
schemes.md
estructura.md
logifresh/
│
├── docker-compose.yml                 # ← Integración final (todos juntos)
├── .gitignore                         # ← Ignorar node_modules, .env, etc.
│
├── shared/                            # ← Opcional: tipos comunes (si deciden unificar)
│   └── types/
│       ├── Pedido.ts
│       ├── Factura.ts
│       └── index.ts
│
├── servicios/                         # ← Carpeta raíz de todos los microservicios
│   │
│   ├── pedidos/                       # ← Persona 1
│   │   ├── Dockerfile
│   │   ├── docker-compose.dev.yml     # ← Para pruebas individuales
│   │   ├── .env.example
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── src/
│   │       ├── index.ts
│   │       ├── app.ts
│   │       ├── controllers/
│   │       │   └── pedidoController.ts
│   │       ├── models/
│   │       │   └── Pedido.ts
│   │       ├── services/
│   │       │   ├── inventarioClient.ts
│   │       │   ├── facturacionClient.ts
│   │       │   ├── transporteClient.ts
│   │       │   └── notificacionesClient.ts
│   │       └── utils/
│   │           └── logger.ts
│   │
│   ├── inventario/                    # ← Persona 2
│   │   ├── Dockerfile
│   │   ├── docker-compose.dev.yml
│   │   ├── .env.example
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── src/
│   │       ├── index.ts
│   │       ├── app.ts
│   │       ├── controllers/
│   │       │   └── inventarioController.ts
│   │       ├── services/
│   │       │   └── redisClient.ts
│   │       ├── data/
│   │       │   └── stockInicial.ts    # ← Datos precargados
│   │       └── utils/
│   │           └── logger.ts
│   │
│   ├── facturacion/                   # ← Persona 3
│   │   ├── Dockerfile
│   │   ├── docker-compose.dev.yml
│   │   ├── .env.example
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── src/
│   │       ├── index.ts
│   │       ├── app.ts
│   │       ├── controllers/
│   │       │   └── facturaController.ts
│   │       ├── entities/
│   │       │   ├── Factura.ts
│   │       │   └── ItemFactura.ts
│   │       ├── services/
│   │       │   └── promocionService.ts
│   │       ├── data-source.ts        # ← TypeORM config
│   │       └── utils/
│   │           └── logger.ts
│   │
│   ├── transporte/                    # ← Persona 4
│   │   ├── Dockerfile
│   │   ├── docker-compose.dev.yml
│   │   ├── .env.example
│   │   ├── .dockerignore
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── src/
│   │       ├── index.ts
│   │       ├── app.ts
│   │       ├── controllers/
│   │       │   └── transporteController.ts
│   │       ├── services/
│   │       │   ├── redisClient.ts
│   │       │   └── queueService.ts   # ← Bull o lista simple
│   │       ├── data/
│   │       │   └── camiones.ts        # ← Lista de camiones
│   │       └── utils/
│   │           └── logger.ts
│   │
│   └── notificaciones/                # ← Persona 5
│       ├── Dockerfile
│       ├── docker-compose.dev.yml
│       ├── .env.example
│       ├── .dockerignore
│       ├── package.json
│       ├── tsconfig.json
│       └── src/
│           ├── index.ts
│           ├── app.ts
│           ├── controllers/
│           │   └── notificacionesController.ts
│           ├── services/
│           │   └── emailService.ts    # ← console.log + fallos aleatorios
│           ├── data/
│           │   └── historial.ts       # ← Array en memoria
│           └── utils/
│               └── logger.ts
│
└── pruebas/                           # ← Opcional: scripts de prueba
    ├── k6/
    │   └── test.js                    # ← Prueba de rendimiento
    ├── postman/
    │   └── LogiFresh.postman_collection.json
    └── casos-de-prueba/
        └── casos.xlsx
```

---

## 📄 Archivos Comunes para Copiar en Cada Servicio

### `package.json` 

```json
{
  "name": "logifresh-[servicio]",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "dev": "tsx src/index.ts",
    "build": "tsc",
    "start": "node dist/index.js",
    "clean": "rm -rf dist",
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [],
  "author": "",
  "license": "ISC",
  "type": "module",
  "devDependencies": {
    "@types/cors": "2.8.19",
    "@types/express": "5.0.6",
    "@types/node": "25.6.0",
    "@types/pg": "8.20.0",
    "tsx": "4.21.0",
    "typescript": "6.0.3"
  },
  "dependencies": {
    "cors": "2.8.6",
    "dotenv": "17.4.2",
    "express": "5.2.1",
    "pg": "8.21.0" //solo para el que usa postgress
  }
}
```

*Cada persona agregará sus dependencias específicas (mongoose, redis, typeorm, etc.)*
---

### `Dockerfile` (idéntico para todos)

```dockerfile
FROM node:26-alpine

WORKDIR /app

# Copiar archivos de configuración
COPY package*.json ./
COPY tsconfig.json ./

# Instalar dependencias
RUN npm install

# Copiar código fuente
COPY src ./src

# Exponer puerto (se define en docker-compose)
EXPOSE ${PORT}

# Comando para desarrollo
CMD ["npm", "run", "dev"]
```

---

### `.env.example` (para que cada uno lo copie a `.env`)

```env
# Configuración del servicio
PORT=8081
NODE_ENV=development

# URLs de otros servicios (en desarrollo local)
INVENTARIO_URL=http://localhost:8082
FACTURACION_URL=http://localhost:8083
TRANSPORTE_URL=http://localhost:8084
NOTIFICACIONES_URL=http://localhost:8085

# Configuración de BD (según cada servicio)
MONGO_URI=mongodb://localhost:27017/pedidos
REDIS_HOST=localhost
REDIS_PORT=6379
DB_HOST=localhost
DB_PORT=5432
DB_USER=admin
DB_PASSWORD=secret
DB_NAME=logifresh
```

---

### `.dockerignore` 

```
node_modules
dist
.env
.git
.gitignore
*.log
.DS_Store
```

---
## .gitignore (raíz)

```
# Dependencias
node_modules/
dist/

# Variables de entorno
.env
.env.local
.env.*.local

# Logs
*.log
npm-debug.log*

# OS
.DS_Store
Thumbs.db

# IDE
.vscode/
.idea/

# Docker
*.pid
```

---

## ✅ Checklist de Estructura para Cada Miembro

- [ ] Carpeta con el nombre de su servicio dentro de `servicios/`
- [ ] `Dockerfile` (el mismo para todos)
- [ ] `docker-compose.dev.yml` (con su BD local)
- [ ] `.env.example` (para compartir)
- [ ] `.dockerignore`
- [ ] `package.json` (con sus dependencias adicionales)
- [ ] `tsconfig.json` (el mismo)
- [ ] `src/` con `index.ts` y `app.ts`
- [ ] Probar que su servicio funciona solo con `docker-compose.dev.yml`
- [ ] Subir a Git **sin el `.env`** (solo `.env.example`)


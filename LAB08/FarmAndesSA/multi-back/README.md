# Sistema de Inventario Distribuido - Transferencias entre Almacenes

Sistema de gestión de inventario para múltiples almacenes (Arequipa, Lima, Cusco) que permite realizar transferencias de productos entre sedes utilizando un protocolo Two-Phase Commit (2PC) para garantizar atomicidad en las operaciones distribuidas.

## Arquitectura

~~~shell
┌─────────────────────────────────────────────────────────────┐
│                      DOCKER NETWORK                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │   Arequipa   │    │     Lima     │    │    Cusco     │   │
│  ├──────────────┤    ├──────────────┤    ├──────────────┤   │
│  │ Backend:3001 │    │ Backend:3002 │    │ Backend:3003 │   │
│  │ Postgres:5432│    │ Postgres:5433│    │ Postgres:5434│   │
│  └──────┬───────┘    └──────┬───────┘    └──────┬───────┘   │
│         │                   │                   │           │
│         └───────────────────┼───────────────────┘           │
│                             │                               │
│                    Red Interna Docker                       │
│              (backend-arequipa:3000, etc.)                  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    Cliente (curl / Postman)
~~~

## Requisitos Previos

- Docker y Docker Compose instalados
- Git (opcional, para clonar el repositorio)
- Puertos libres: 3001, 3002, 3003 (backends) y 5432, 5433, 5434 (bases de datos)

## Instalación y Ejecución

Despues de bajar los cambios del repositorio

tenemos la siguiente arquitectura

~~~bash
multi-back/
├── src/
│   ├── config.ts
│   ├── coordinator.ts
│   ├── index.ts
│   ├── pool.ts
│   ├── queries.ts
│   └── types.d.ts
├── init-sql/
│   └── 01-init.sql
├── Dockerfile
├── docker-compose.yml
├── package.json
├── tsconfig.json
└── README.md
~~~

### Construir y levantar los contenedores
bash

Construir imágenes y levantar todos los servicios

~~~
docker compose up -d --build
~~~

Qué hace este comando:

- Construye las imágenes de los backends (compila TypeScript)
- Crea 3 contenedores de PostgreSQL (uno por almacén)
- Crea 3 contenedores de backend (uno por almacén)
- Configura la red interna para que se comuniquen
- Inicia todo en segundo plano (-d)

### Verificar que todo funciona

Estos comandos pueden necesitar permiso del administrador:
- sudo docker ...

~~~bash
# Ver estado de los contenedores
docker compose ps

# Ver logs de un nodo específico
docker compose logs backend-arequipa
docker compose logs backend-lima
docker compose logs backend-cusco

# Ver logs de todos
docker compose logs -f
~~~

### Probar conectividad

~~~bash
# Verificar que cada nodo está sano
curl http://localhost:3001/health
curl http://localhost:3002/health
curl http://localhost:3003/health
~~~

respuesta esperada

~~~json
{"city":"arequipa","status":"healthy","db":"inventario_arequipa","timestamp":"..."}
~~~

## Endpoints Disponibles

### Endpoints Públicos (para el cliente)

|Método|Endpoint|Descripción|Ejemplo|
|---|---|---|---|
|GET|/health|Verificar estado del nodo|curl http://localhost:3001/health|
|GET|/inventario|Ver todo el inventario del almacén|curl http://localhost:3001/inventario|
|GET	|/inventario/:producto	|Ver stock de un producto específico	|curl http://localhost:3001/inventario/Paracetamol|
|POST	|/inventario	|Crear o actualizar producto	|curl -X POST ... -d '{"producto":"Aspirina","stock":100}'|
|POST	|/transferir	|Transferir productos a otro almacén	|Ver ejemplo abajo|

### Endpoint Principal: Transferencia

~~~bash
curl -X POST http://localhost:3001/transferir \
  -H "Content-Type: application/json" \
  -d '{
    "producto": "Paracetamol",
    "cantidad": 10,
    "destino": "lima"
  }'
~~~

Respuesta exitosa:

~~~json
{
  "success": true,
  "message": "Transferencia exitosa: 10 unidad(es) de \"Paracetamol\" de arequipa a lima",
  "details": {
    "transactionId": "1734567890123-abc123",
    "producto": "Paracetamol",
    "cantidad": 10,
    "origen": "arequipa",
    "destino": "lima"
  }
}
~~~

Respuesta con error (stock insuficiente):

~~~json
{
  "success": false,
  "message": "Stock insuficiente en arequipa. Disponible: 5, solicitado: 10"
}
~~~

Respuesta con error (nodo caído):

~~~json
{
  "success": false,
  "message": "El almacén de lima no respondió en el tiempo esperado (5s). Intente nuevamente."
}
~~~

### Endpoints Internos (entre nodos - 2PC)

|Método|Endpoint|Uso|
|---|---|---|
|POST|/api/2pc/prepare|Fase 1: Destino verifica que puede recibir|
|POST|/api/2pc/commit|Fase 2: Destino confirma la suma de stock|
|POST|/api/2pc/rollback|Cancelar transacción (fallo)|

Estos endpoints son solo para comunicación entre backends, no para uso directo del cliente.

Cómo Funciona la Transferencia (Two-Phase Commit)
~~~text
Cliente                Origen (Arequipa)              Destino (Lima)
   │                         │                              │
   │──POST /transferir──────→│                              │
   │                         │                              │
   │                    ┌────┴────┐                         │
   │                    │ FASE 1  │                         │
   │                    │ PREPARE │                         │
   │                    └────┬────┘                         │
   │                         │                              │
   │                         │──POST /api/2pc/prepare──────→│
   │                         │                              │
   │                         │    ┌─────────────────────┐   │
   │                         │    │ Verifica que puede  │   │
   │                         │    │ recibir el producto │   │
   │                         │    └─────────────────────┘   │
   │                         │                              │
   │                         │←───── { success: true }──────│
   │                         │                              │
   │                    ┌────┴────┐                         │
   │                    │ FASE 2  │                         │
   │                    │ COMMIT  │                         │
   │                    └────┬────┘                         │
   │                         │                              │
   │                         │──POST /api/2pc/commit───────→│
   │                         │                              │
   │                         │    ┌─────────────────────┐   │
   │                         │    │ Suma stock al       │   │
   │                         │    │ destino             │   │
   │                         │    └─────────────────────┘   │
   │                         │                              │
   │                         │←───── { success: true }──────│
   │                         │                              │
   │                         │    ┌─────────────────────┐   │
   │                         │    │ Confirma descuento  │   │
   │                         │    │ en origen           │   │
   │                         │    └─────────────────────┘   │
   │                         │                              │
   │←───── Respuesta ────────│                              │
   │                         │                              │
~~~

### ¿Qué pasa si algo falla?

- Escenario	Acción	Respuesta al usuario
- Origen sin stock	Rollback inmediato	"Stock insuficiente"
- Destino no responde (timeout 5s)	Rollback en origen	"Destino no disponible"
- Destino rechaza Prepare	Rollback en origen	"Destino rechazó la operación"
- Commit en destino falla	Rollback en origen	"Error confirmando en destino"

## Comandos Útiles de Docker

### Gestión de Contenedores

~~~bash
# Ver todos los contenedores (incluye los que no están corriendo)
docker ps -a

# Ver solo los contenedores del proyecto actual
docker compose ps

# Ver logs en tiempo real de todos los servicios
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs backend-arequipa -f

# Ver logs de PostgreSQL
docker compose logs postgres-arequipa
~~~

### Detener y Reiniciar

~~~bash
# Detener todos los contenedores (los datos persisten)
docker compose stop

# Iniciar contenedores detenidos
docker compose start

# Reiniciar un servicio específico
docker compose restart backend-lima

# Pausar contenedores (congela el estado)
docker compose pause

# Reanudar contenedores pausados
docker compose unpause
~~~

### Reconstrucción

~~~bash
# Reconstruir imágenes y levantar (después de cambios en código)
docker compose up -d --build

# Reconstruir sin usar caché (solución de problemas)
docker compose build --no-cache

# Reconstruir solo un servicio
docker compose build backend-arequipa
docker compose up -d
~~~

### Comandos dentro de Contenedores

~~~bash
# Acceder al shell de un backend
docker exec -it backend-arequipa sh

# Ejecutar consulta SQL directamente en una BD
docker exec -it postgres-arequipa psql -U admin -d inventario_arequipa -c "SELECT * FROM inventario;"

# Ver inventario desde el host
docker exec backend-arequipa curl http://localhost:3000/inventario
~~~

## Eliminación y Persistencia de Datos

### Datos que PERSISTEN

~~~bash
# Detener contenedores (los datos NO se pierden)
docker compose stop

# Iniciar de nuevo (recupera los datos anteriores)
docker compose start
~~~

Los datos de las bases de datos se guardan en volúmenes Docker:
- postgres_data_arequipa
- postgres_data_lima
- postgres_data_cusco

### Datos que SE PIERDEN

~~~bash
# Eliminar contenedores (mantiene volúmenes)
docker compose down

# Eliminar TODO (contenedores + volúmenes + datos)
docker compose down -v
~~~

|Comando	Contenedores|Redes|Volúmenes (datos)|
|---|---|---|
|docker compose stop	|❌ No se eliminan	|✅ Persisten	|✅ Persisten|
|docker compose down	|✅ Eliminados|	✅ Eliminadas	|✅ Persisten|
|docker compose down -v	|✅ Eliminados	|✅ Eliminadas	|❌ Eliminados|

### Limpieza Completa

~~~bash
# 1. Detener y eliminar contenedores + volúmenes
docker compose down -v

# 2. Eliminar imágenes construidas
docker rmi multi-back-backend

# 3. Limpiar recursos no usados del sistema
docker system prune -a
~~~

## Ejemplos de Prueba

1. Ver inventario inicial de cada almacén

~~~bash
# Arequipa
curl http://localhost:3001/inventario 

# Lima
curl http://localhost:3002/inventario 

# Cusco
curl http://localhost:3003/inventario 
~~~

2. Transferir de Arequipa a Lima

~~~bash
curl -X POST http://localhost:3001/transferir \
  -H "Content-Type: application/json" \
  -d '{"producto": "Paracetamol", "cantidad": 10, "destino": "lima"}'
~~~

3. Verificar que el stock cambió

~~~bash
# Verificar que Arequipa disminuyó
curl http://localhost:3001/inventario/Paracetamol

# Verificar que Lima aumentó
curl http://localhost:3002/inventario/Paracetamol
~~~

4. Probar con stock insuficiente

~~~bash
curl -X POST http://localhost:3001/transferir \
  -H "Content-Type: application/json" \
  -d '{"producto": "Paracetamol", "cantidad": 1000, "destino": "cusco"}'
~~~

5. Probar tolerancia a fallos (apagar un nodo)

~~~bash
# Apagar Lima
docker stop backend-lima

# Intentar transferencia (fallará)
curl -X POST http://localhost:3001/transferir \
  -H "Content-Type: application/json" \
  -d '{"producto": "Paracetamol", "cantidad": 5, "destino": "lima"}'

# Encender Lima de nuevo
docker start backend-lima
~~~

6. Crear un nuevo producto

~~~bash
curl -X POST http://localhost:3001/inventario \
  -H "Content-Type: application/json" \
  -d '{"producto": "Aspirina", "stock": 50}'
~~~

### Ajustar Timeouts

En docker-compose.yml, modifica:

~~~yaml
backend-arequipa:
  environment:
    PREPARE_TIMEOUT: 10000  # 10 segundos
    COMMIT_TIMEOUT: 10000
~~~

## Solución de Problemas

Error: puerto ya en uso

~~~bash
# Ver qué está usando el puerto
sudo lsof -i :3001

# Cambiar puerto en docker-compose.yml
ports:
  - "3010:3000"  # Cambiar puerto host
~~~

Error: no se puede conectar a PostgreSQL

~~~bash
# Verificar que PostgreSQL está sano
docker compose ps
docker compose logs postgres-arequipa

# Reiniciar la base de datos
docker compose restart postgres-arequipa
~~~

Error: timeout en transferencias

~~~bash
# Aumentar timeouts en docker-compose.yml
PREPARE_TIMEOUT: 10000
COMMIT_TIMEOUT: 10000

# Reconstruir
docker compose up -d --build

Limpiar todo y empezar de cero
bash

docker compose down -v
docker compose up -d --build
~~~

## Diagrama de Estados de una Transacción

~~~text
                    ┌─────────────────┐
                    │   IDLE (inicio) │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │   PREPARING     │
                    │  (origen y      │
                    │   destino)      │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
         ┌────▼────┐    ┌─────▼─────┐  ┌─────▼─────┐
         │PREPARE  │    │ PREPARE   │  │ TIMEOUT/  │
         │  OK     │    │  FAIL     │  │ ERROR     │
         └────┬────┘    └─────┬─────┘  └─────┬─────┘
              │               │              │
         ┌────▼────┐          │              │
         │COMMIT   │          │              │
         │FASE     │          │              │
         └────┬────┘          │              │
              │               │              │
         ┌────▼────┐     ┌────▼────┐   ┌─────▼─────┐
         │COMMIT   │     │ROLLBACK │   │ ROLLBACK  │
         │  OK     │     │en origen│   │ en origen │
         └────┬────┘     └────┬────┘   └─────┬─────┘
              │               │              │
         ┌────▼────┐     ┌────▼────┐   ┌─────▼─────┐
         │COMPLETE │     │FAILED   │   │  FAILED   │
         │(éxito)  │     │(stock)  │   │(nodo down)│
         └─────────┘     └─────────┘   └───────────┘
~~~

## Notas Adicionales

- Los scripts SQL se ejecutan solo la primera vez que se crea el volumen
- Para reiniciar datos de prueba: docker compose down -v && docker compose up -d
- Los backends usan tsx para hot-reload durante desarrollo
- La comunicación entre nodos es síncrona con timeouts configurables

Con esto deberia bastar para entender el back, igual deben revisar el code :3

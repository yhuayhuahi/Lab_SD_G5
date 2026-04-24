# Instrucciones de ejecucion - Algoritmo de Berkeley

## 1. Requisitos

- Node.js 20 o superior.
- npm (incluido con Node.js).
- Sistema operativo: Windows, Linux o macOS.

## 2. Instalacion

Desde la carpeta del proyecto `Alg_berkeley` ejecuta:

```bash
npm install
```

## 3. Ejecucion rapida (recomendada)

Ejecuta una sola ronda completa de sincronizacion:

```bash
npm run dev
```

Este comando hace lo siguiente automaticamente:

1. Levanta el Master en `localhost:3000`.
2. Levanta 3 nodos clientes.
3. Cada nodo envia su tiempo local al Master.
4. El Master calcula los offsets y envia los ajustes.
5. Se imprime el reloj final del Master y de cada nodo.
6. Todos los procesos se cierran solos al terminar.

## 4. Valores actuales de la simulacion

En `src/index.ts` actualmente se usa:

- Reloj base: `10000` ms.
- Desfase inicial de nodos:
- `node1`: `-5` ms
- `node2`: `10` ms
- `node3`: `15` ms

## 5. Ejecucion manual (opcional)

Si quieres correr cada proceso por separado, usa 4 terminales.

Terminal 1 (Master):

```bash
npx tsx src/BerkeleyMaster.ts -n master --time=10000
```

Terminal 2 (Nodo 1):

```bash
npx tsx src/BerkeleyNode.ts -n node1 --time=9995
```

Terminal 3 (Nodo 2):

```bash
npx tsx src/BerkeleyNode.ts -n node2 --time=10010
```

Terminal 4 (Nodo 3):

```bash
npx tsx src/BerkeleyNode.ts -n node3 --time=10015
```

## 6. Errores comunes

### ECONNREFUSED 127.0.0.1:3000

Significa que un nodo intento conectarse antes de que el Master estuviera listo.

Solucion:

- Usa `npm run dev` (arranque orquestado), o
- Inicia primero el Master y luego los nodos.

### ERR_PARSE_ARGS_INVALID_OPTION_VALUE con valores negativos

Si usas un valor negativo en `time`, debe ir asi:

```bash
--time=-5
```

No usar separacion tipo `-t -5`.

## 7. Resultado esperado

Al final debes ver lineas similares a estas:

- `Reloj final del master: ...`
- `Reloj final de node1: ...`
- `Reloj final de node2: ...`
- `Reloj final de node3: ...`

Los relojes finales deben quedar muy cercanos entre si (en ms).

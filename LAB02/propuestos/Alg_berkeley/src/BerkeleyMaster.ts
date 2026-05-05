import net from "node:net"
import { argv } from "node:process"
import { parseArgs } from "node:util"

const PORT = 3000                  
const EXPECTED_NODES = 3
const ROUND_WAIT_MS = 5000
const FINALIZE_DELAY_MS = 300

const args = argv.slice(2) 

// Define los argumentos aceptados: nombre del master y tiempo base simulado
const options: ParameterOptions = {
  name: { type: "string", short: "n" },  
  time: { type: "string", short: "t" } 
} as const

const { values } = parseArgs({ args, options }) 

// Reloj simulado del master: tiempo base por argumento más el tiempo real transcurrido
// masterCorrectionMs acumula el ajuste calculado tras cada ronda
const serverName = values.name ?? "master" 
const parsedMasterTime = Number(values.time ?? Date.now().toString())  
const baseMasterTimeMs = Number.isFinite(parsedMasterTime) 
  ? parsedMasterTime
  : Date.now()
const realStartMs = Date.now()
let masterCorrectionMs = 0

// Devuelve la hora actual del master aplicando el avance real y la corrección acumulada
const getMasterTimeMs = () =>
  baseMasterTimeMs + (Date.now() - realStartMs) + masterCorrectionMs

// Estado de cada nodo conectado: socket, buffer de datos incompletos,
// y los tres timestamps necesarios para calcular latencia y offset
type ClientState = {
  socket: net.Socket
  buffer: string 
  requestSentAt?: number 
  responseReceivedAt?: number 
  clientTimeMs?: number 
}

// Mapa de clientes activos. Clave: "ip:puerto" del nodo
const clients = new Map<string, ClientState>()
let roundStarted = false 
let finalized = false

// Elimina el prefijo ::ffff: que Node agrega a direcciones IPv4 en sockets IPv6
const normalizeAddress = (address: string | undefined) =>
  (address ?? "unknown").replace(/^::ffff:/, "")

// Cierra todos los sockets y detiene el servidor. Se llama una sola vez al final de la ronda
const finalizeRound = () => {
  if (finalized) return
  finalized = true

  for (const state of clients.values()) {
    state.socket.end()
  }

  server.close(() => {
    process.exit(0)
  })
}

// Inicia la ronda cuando se conectan todos los nodos esperados:
// 1. Envía "get" a cada nodo registrando el momento exacto del envío
// 2. Espera ROUND_WAIT_MS para recibir todas las respuestas
// 3. Calcula el offset de cada nodo compensando la latencia de red
// 4. Calcula el promedio global y envía el ajuste individual a cada nodo
const tryStartRound = () => {
  if (roundStarted || clients.size < EXPECTED_NODES) {
    return
  }

  roundStarted = true
  console.log(`Se alcanzaron ${EXPECTED_NODES} nodos. Iniciando consulta de tiempo...`)

  for (const [clientId, state] of clients.entries()) {
    const requestSentAt = getMasterTimeMs()
    state.requestSentAt = requestSentAt
    state.responseReceivedAt = undefined
    state.clientTimeMs = undefined

    const request: BerkeleyData = {
      type: "get",
      info: "Master solicita hora local",
      time: String(requestSentAt),
      name: serverName
    }

    state.socket.write(`${JSON.stringify(request)}\n`)
    console.log(`Solicitud enviada a ${clientId}`)
  }

  setTimeout(() => {
    const offsets: Array<{ clientId: string; offsetMs: number }> = []

    for (const [clientId, state] of clients.entries()) {
      if (
        state.requestSentAt === undefined ||
        state.responseReceivedAt === undefined ||
        state.clientTimeMs === undefined
      ) {
        console.warn(`Sin respuesta completa de ${clientId}`)
        continue
      }

      // Estimación de latencia: asume ida y vuelta simétricas
      // offsetMs = diferencia entre lo que reportó el nodo y lo que el master estimaba
      const t0 = state.requestSentAt
      const t1 = state.responseReceivedAt
      const roundTripLatencyMs = t1 - t0
      const oneWayLatencyMs = roundTripLatencyMs / 2
      const serverEstimate = t0 + oneWayLatencyMs
      const offsetMs = state.clientTimeMs - serverEstimate
      offsets.push({ clientId, offsetMs })
    }

    // Si no respondieron todos los nodos, cancela la ronda sin ajustar nada
    if (offsets.length !== EXPECTED_NODES) {
      console.warn(
        `No llegaron todas las respuestas (${offsets.length}/${EXPECTED_NODES}). Ronda cancelada.`
      )
      finalizeRound()
      return
    }

    // Promedio de offsets incluyendo al master (su offset propio es 0)
    // Cada participante recibe: promedio - su_offset como ajuste
    const masterOffsetMs = 0
    const totalOffset = offsets.reduce((sum, item) => sum + item.offsetMs, 0) + masterOffsetMs
    const totalParticipants = offsets.length + 1
    const averageOffset = totalOffset / totalParticipants

    const masterAdjustmentMs = Math.round(averageOffset - masterOffsetMs)
    masterCorrectionMs += masterAdjustmentMs
    console.log(
      `Ajuste local del master: ${masterAdjustmentMs} ms. Correccion acumulada: ${masterCorrectionMs} ms`
    )
    console.log(`Reloj final del master: ${Math.round(getMasterTimeMs())}`)

    // Envía a cada nodo cuánto debe ajustar su reloj
    for (const { clientId, offsetMs } of offsets) {
      const adjustmentMs = Math.round(averageOffset - offsetMs)
      const state = clients.get(clientId)
      if (!state) continue

      const adjustment: BerkeleyData = {
        type: "post",
        info: "Ajuste de reloj",
        time: String(adjustmentMs),
        name: serverName
      }

      state.socket.write(`${JSON.stringify(adjustment)}\n`)
      console.log(`Ajuste enviado a ${clientId}: ${adjustmentMs} ms`)
    }

    // Pequeño delay para asegurar que los ajustes llegaron antes de cerrar
    setTimeout(() => {
      finalizeRound()
    }, FINALIZE_DELAY_MS)
  }, ROUND_WAIT_MS)
}

// Acepta conexiones de nodos. Por cada nodo:
// - Lo registra en el mapa de clientes
// - Inicia la ronda si ya están todos conectados
// - Escucha sus mensajes acumulando en buffer y procesando por línea
// - Registra responseReceivedAt y clientTimeMs cuando llega el "post" con su hora
const server = net.createServer((socket) => {
  const clientIp = normalizeAddress(socket.remoteAddress)
  const clientId = `${clientIp}:${socket.remotePort ?? 0}`
  clients.set(clientId, { socket, buffer: "" })
  console.log(`Nodo conectado: ${clientId} (${clients.size}/${EXPECTED_NODES})`)

  if (clients.size === EXPECTED_NODES) {
    tryStartRound()
  }

  // Acumula chunks TCP y procesa mensajes completos separados por \n
  socket.on("data", (chunk) => {
    const state = clients.get(clientId)
    if (!state) return

    state.buffer += chunk.toString()

    let newlineIndex = state.buffer.indexOf("\n")
    while (newlineIndex >= 0) {
      const rawMessage = state.buffer.slice(0, newlineIndex).trim()
      state.buffer = state.buffer.slice(newlineIndex + 1)

      if (rawMessage) {
        try {
          const payload = JSON.parse(rawMessage) as BerkeleyData
          if (payload.type !== "post") {
            console.warn(`Mensaje inesperado de ${clientId}: ${payload.type}`)
          } else {
            // Guarda el instante de recepción y la hora que reportó el nodo
            const responseReceivedAt = getMasterTimeMs()
            const clientTimeMs = Number(payload.time ?? NaN)

            if (!Number.isFinite(clientTimeMs)) {
              console.warn(`Tiempo invalido de ${clientId}: ${payload.time}`)
            } else {
              state.responseReceivedAt = responseReceivedAt
              state.clientTimeMs = clientTimeMs
              console.log(`Respuesta valida de ${clientId}`)
            }
          }
        } catch (error) {
          console.warn(`Chunk invalido de ${clientId}: ${rawMessage}`)
        }
      }

      newlineIndex = state.buffer.indexOf("\n")
    }
  })

  // Elimina el nodo del mapa cuando cierra su conexión
  socket.on("end", () => {
    clients.delete(clientId)
    console.log(`Nodo desconectado: ${clientId}`)
  })

  socket.on("error", (error) => {
    console.warn(`Error con ${clientId}: ${error.message}`)
  })
})

// Levanta el servidor TCP y muestra el reloj base con el que arrancó
server.listen(PORT, () => {
  console.log(`Master escuchando en: http://localhost:${PORT}/`)
  console.log(`Reloj base del master: ${baseMasterTimeMs}`)
})
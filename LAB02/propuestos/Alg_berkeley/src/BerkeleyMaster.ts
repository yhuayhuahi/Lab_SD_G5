import net from "node:net"
import { argv } from "node:process"
import { parseArgs } from "node:util"

const PORT = 3000
const EXPECTED_NODES = 3
const ROUND_WAIT_MS = 5000
const FINALIZE_DELAY_MS = 300

const args = argv.slice(2)

const options: ParameterOptions = {
  name: { type: "string", short: "n" },
  time: { type: "string", short: "t" }
} as const

const { values } = parseArgs({ args, options })

const serverName = values.name ?? "master"
const parsedMasterTime = Number(values.time ?? Date.now().toString())
const baseMasterTimeMs = Number.isFinite(parsedMasterTime)
  ? parsedMasterTime
  : Date.now()
const realStartMs = Date.now()
let masterCorrectionMs = 0

const getMasterTimeMs = () =>
  baseMasterTimeMs + (Date.now() - realStartMs) + masterCorrectionMs

type ClientState = {
  socket: net.Socket
  buffer: string
  requestSentAt?: number
  responseReceivedAt?: number
  clientTimeMs?: number
}

const clients = new Map<string, ClientState>()
let roundStarted = false
let finalized = false

const normalizeAddress = (address: string | undefined) =>
  (address ?? "unknown").replace(/^::ffff:/, "")

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

      const t0 = state.requestSentAt
      const t1 = state.responseReceivedAt
      const roundTripLatencyMs = t1 - t0
      const oneWayLatencyMs = roundTripLatencyMs / 2
      const serverEstimate = t0 + oneWayLatencyMs
      const offsetMs = state.clientTimeMs - serverEstimate
      offsets.push({ clientId, offsetMs })
    }

    if (offsets.length !== EXPECTED_NODES) {
      console.warn(
        `No llegaron todas las respuestas (${offsets.length}/${EXPECTED_NODES}). Ronda cancelada.`
      )
      finalizeRound()
      return
    }

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

    setTimeout(() => {
      finalizeRound()
    }, FINALIZE_DELAY_MS)
  }, ROUND_WAIT_MS)
}

const server = net.createServer((socket) => {
  const clientIp = normalizeAddress(socket.remoteAddress)
  const clientId = `${clientIp}:${socket.remotePort ?? 0}`
  clients.set(clientId, { socket, buffer: "" })
  console.log(`Nodo conectado: ${clientId} (${clients.size}/${EXPECTED_NODES})`)

  if (clients.size === EXPECTED_NODES) {
    tryStartRound()
  }

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

  socket.on("end", () => {
    clients.delete(clientId)
    console.log(`Nodo desconectado: ${clientId}`)
  })

  socket.on("error", (error) => {
    console.warn(`Error con ${clientId}: ${error.message}`)
  })
})

server.listen(PORT, () => {
  console.log(`Master escuchando en: http://localhost:${PORT}/`)
  console.log(`Reloj base del master: ${baseMasterTimeMs}`)
})
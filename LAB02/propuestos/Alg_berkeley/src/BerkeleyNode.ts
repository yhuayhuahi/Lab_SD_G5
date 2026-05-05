import net from "node:net"
import { argv } from "node:process"
import { parseArgs } from "node:util"

const HOST = "127.0.0.1"
const PORT = 3000

const args = argv.slice(2)

// Define los argumentos aceptados: nombre del nodo y tiempo base
const options: ParameterOptions = {
  name: { type: "string", short: "n" },
  time: { type: "string", short: "t" }
} as const

const { values } = parseArgs({ args, options })

// Reloj simulado: tiempo base recibido por argumento más el avance real transcurrido
// correctionMs acumula el ajuste que mande el master, se suma al tiempo base para simular un reloj que puede ir más rápido o más lento que el real, y se va ajustando a medida que se reciben correcciones del master
const nodeName = values.name ?? "node"
const parsedBaseTime = Number(values.time ?? Date.now().toString())
const baseNodeTimeMs = Number.isFinite(parsedBaseTime)
  ? parsedBaseTime
  : Date.now()
const realStartMs = Date.now()
let correctionMs = 0
let finished = false

// Abre la conexión TCP al master. Cuando conecta, lo notifica por consola
const socket = net.createConnection({ host: HOST, port: PORT }, () => {
  console.log(`Nodo ${nodeName} conectado a ${HOST}:${PORT}`)
})

let buffer = ""

// Devuelve la hora local actual del nodo aplicando el offset simulado y la corrección acumulada
const getLocalTimeMs = () =>
  baseNodeTimeMs + (Date.now() - realStartMs) + correctionMs

// Recibe mensajes del master. Acumula en buffer y procesa de a un mensaje por línea
// Si es "get": responde con la hora local actual
// Si es "post": aplica el ajuste al reloj y cierra la conexión
socket.on("data", (data) => {
  buffer += data.toString()

  let newlineIndex = buffer.indexOf("\n")
  while (newlineIndex >= 0) {
    const raw = buffer.slice(0, newlineIndex).trim()
    buffer = buffer.slice(newlineIndex + 1)

    if (raw) {
      try {
        const payload = JSON.parse(raw) as BerkeleyData

        if (payload.type === "get") { 
          const reply: BerkeleyData = {
            type: "post",
            info: `Hora local de ${nodeName}`,
            time: String(getLocalTimeMs()),
            name: nodeName
          }

          socket.write(`${JSON.stringify(reply)}\n`)
        } else if (payload.type === "post") {
          const adjustmentMs = Number(payload.time ?? NaN)
          if (Number.isFinite(adjustmentMs)) {
            correctionMs += adjustmentMs
            console.log(
              `Ajuste recibido (${adjustmentMs} ms). Correccion acumulada: ${correctionMs} ms`
            )
            console.log(`Reloj final de ${nodeName}: ${Math.round(getLocalTimeMs())}`)

            // Evita procesar un segundo ajuste y cierra la conexión limpiamente
            if (!finished) {
              finished = true
              socket.end()
            }
          } else { // ejm de ajuste inválido: time no es un número finito
            console.warn(`Ajuste invalido: ${payload.time}`)
          }
        }
      } catch (error) { // ejm de mensaje que no es un JSON válido, se daría un error al intentar parsearlo, se captura y se muestra una advertencia
        console.warn(`Mensaje invalido: ${raw}`)
      }
    }

    newlineIndex = buffer.indexOf("\n")
  }
})

// El master cerró su lado de la conexión
socket.on("end", () => {
  console.log("Conexion finalizada por el master")
})

// Error de red: termina el proceso con código de falla
socket.on("error", (error) => {
  console.warn(`Error de conexion: ${error.message}`)
  process.exit(1)
})

// Socket completamente cerrado: termina el proceso limpiamente
socket.on("close", () => {
  process.exit(0)
})
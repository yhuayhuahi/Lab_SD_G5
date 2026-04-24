import net from "node:net"
import { argv } from "node:process"
import { parseArgs } from "node:util"

const HOST = "127.0.0.1"
const PORT = 3000

const args = argv.slice(2)

const options: ParameterOptions = {
	name: { type: "string", short: "n" },
	time: { type: "string", short: "t" }
} as const

const { values } = parseArgs({ args, options })

const nodeName = values.name ?? "node"
const parsedBaseTime = Number(values.time ?? Date.now().toString())
const baseNodeTimeMs = Number.isFinite(parsedBaseTime)
  ? parsedBaseTime
  : Date.now()
const realStartMs = Date.now()
let correctionMs = 0
let finished = false

const socket = net.createConnection({ host: HOST, port: PORT }, () => {
	console.log(`Nodo ${nodeName} conectado a ${HOST}:${PORT}`)
})

let buffer = ""

const getLocalTimeMs = () =>
	baseNodeTimeMs + (Date.now() - realStartMs) + correctionMs

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

						if (!finished) {
							finished = true
							socket.end()
						}
					} else {
						console.warn(`Ajuste invalido: ${payload.time}`)
					}
				}
			} catch (error) {
				console.warn(`Mensaje invalido: ${raw}`)
			}
		}

		newlineIndex = buffer.indexOf("\n")
	}
})

socket.on("end", () => {
	console.log("Conexion finalizada por el master")
})

socket.on("error", (error) => {
	console.warn(`Error de conexion: ${error.message}`)
	process.exit(1)
})

socket.on("close", () => {
	process.exit(0)
})

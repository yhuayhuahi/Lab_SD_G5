import { spawn } from "node:child_process"

// Define la forma de cada nodo: nombre e offset para simular desincronización
type NodeConfig = {
  name: string
  offsetMs: number
}

// Tiempo base compartido para master y nodos. Los offsets se aplican sobre este valor
const BASE_CLOCK_MS = 10000

// Nodos con relojes desincronizados intencionalmente para probar el algoritmo
const nodes: NodeConfig[] = [
  { name: "node1", offsetMs: -5 },
  { name: "node2", offsetMs: 10 },
  { name: "node3", offsetMs: 15 }
]

const baseNow = BASE_CLOCK_MS

// Guarda referencias a los procesos hijos para poder matarlos si es necesario, como cuando el usuario interrumpe con Ctrl+C
const children: Array<ReturnType<typeof spawn>> = []
const totalProcesses = nodes.length + 1
let finishedChildren = 0
let hasFailure = false

// Lanza un proceso hijo con npx tsx. Cuando termina, acumula si hubo error
// y cierra el padre cuando todos los hijos hayan finalizado
const spawnProcess = (label: string, args: string[]) => {
  const child = spawn("npx", ["tsx", ...args], {
    stdio: "inherit",
    shell: true
  })

  children.push(child)
  child.on("exit", (code, signal) => {
    finishedChildren += 1

    if (code !== 0 || signal) {
      hasFailure = true
      console.warn(
        `${label} termino con error (codigo: ${code ?? "null"}, signal: ${signal ?? "null"})`
      )
    }

    if (finishedChildren === totalProcesses) {
      process.exit(hasFailure ? 1 : 0)
    }
  })
}

// Arranca el master primero para que esté escuchando antes de que lleguen los nodos
spawnProcess("master", [
  "src/BerkeleyMaster.ts",
  "-n",
  "master",
  `--time=${baseNow}`
])

// Espera 1500ms para dar tiempo al master de levantar el servidor, luego lanza los nodos
// Cada nodo recibe su tiempo base más su offset para simular relojes distintos
setTimeout(() => {
  for (const node of nodes) {
    spawnProcess("node", [
      "src/BerkeleyNode.ts",
      "-n",
      node.name,
      `--time=${baseNow + node.offsetMs}`
    ])
  }
}, 1500)

// Mata todos los procesos hijos si el usuario interrumpe con Ctrl+C
const shutdown = () => {
  for (const child of children) {
    child.kill()
  }
}

process.on("SIGINT", () => {
  shutdown()
  process.exit(0)
})
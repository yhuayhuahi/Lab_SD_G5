import { spawn } from "node:child_process"

type NodeConfig = {
  name: string
  offsetMs: number
}

const BASE_CLOCK_MS = 10000

const nodes: NodeConfig[] = [
  { name: "node1", offsetMs: -5 },
  { name: "node2", offsetMs: 10 },
  { name: "node3", offsetMs: 15 }
]

const baseNow = BASE_CLOCK_MS

const children: Array<ReturnType<typeof spawn>> = []
const totalProcesses = nodes.length + 1
let finishedChildren = 0
let hasFailure = false

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

spawnProcess("master", [
  "src/BerkeleyMaster.ts",
  "-n",
  "master",
  `--time=${baseNow}`
])

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

const shutdown = () => {
  for (const child of children) {
    child.kill()
  }
}

process.on("SIGINT", () => {
  shutdown()
  process.exit(0)
})

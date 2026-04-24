type BerkeleyData = {
  type: "get" | "post"
  info: string
  time?: string
  name?: string
}

type ParameterOptions = {
  name: { type: "string", short: "n" }
  time: { type: "string", short: "t" }
}

type ClientState = {
  socket: net.Socket
  buffer: string
  requestSentAt?: number
  responseReceivedAt?: number
  clientTimeMs?: number
}
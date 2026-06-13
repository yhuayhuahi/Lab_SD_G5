export function logInfo(message: string, meta?: Record<string, unknown>): void {
  if (meta) {
    console.log(`[${new Date().toISOString()}] INFO: ${message}`, meta);
    return;
  }

  console.log(`[${new Date().toISOString()}] INFO: ${message}`);
}

export function logError(message: string, meta?: Record<string, unknown>): void {
  if (meta) {
    console.error(`[${new Date().toISOString()}] ERROR: ${message}`, meta);
    return;
  }

  console.error(`[${new Date().toISOString()}] ERROR: ${message}`);
}
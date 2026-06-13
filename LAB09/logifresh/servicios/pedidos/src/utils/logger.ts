const logger = {
  info: (msg: string, data?: unknown) => {
    console.log(`[INFO] ${new Date().toISOString()} - ${msg}`, data !== undefined ? data : '');
  },
  error: (msg: string, data?: unknown) => {
    console.error(`[ERROR] ${new Date().toISOString()} - ${msg}`, data !== undefined ? data : '');
  },
  warn: (msg: string, data?: unknown) => {
    console.warn(`[WARN] ${new Date().toISOString()} - ${msg}`, data !== undefined ? data : '');
  },
};

export default logger;

// Tipos existentes
// ============ TIPOS PARA INVENTARIO DISTRIBUIDO ============

export interface InventarioItem {
  id: number;
  producto: string;
  stock: number;
}

export interface TransferRequest {
  producto: string;
  cantidad: number;
  destino: string;  // 'arequipa', 'lima', 'cusco'
}

export interface TransferResponse {
  success: boolean;
  message: string;
  details?: any;
}

export interface PrepareRequest {
  producto: string;
  cantidad: number;
  origen: string;
  destino: string;
  transactionId: string;
}

export interface PrepareResponse {
  success: boolean;
  message: string;
  currentStock?: number;
}

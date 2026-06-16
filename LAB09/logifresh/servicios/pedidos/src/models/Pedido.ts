import mongoose, { Document, Schema } from 'mongoose';

export interface IItem {
  productoId: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal?: number;
}

export interface IHistorialEstado {
  estado: string;
  fecha: Date;
}

export interface IPedido extends Document {
  pedidoId: string;
  clienteId: string;
  clienteEmail: string;
  clienteNombre: string;
  clienteDireccion?: string;
  fechaCreacion: Date;
  fechaActualizacion?: Date;
  estado: string;
  items: IItem[];
  promocionId?: string;
  notas?: string;
  subtotal?: number;
  descuento?: number;
  total?: number;
  igv?: number;
  totalConIgv?: number;
  facturaId?: string;
  transportistaId?: string;
  tiempoEstimadoEntregaMin?: number;
  estadoTransporte?: string;
  historialEstados: IHistorialEstado[];
}

const ItemSchema = new Schema<IItem>(
  {
    productoId: { type: String, required: true },
    nombre: { type: String, required: true },
    cantidad: { type: Number, required: true },
    precioUnitario: { type: Number, required: true },
    subtotal: Number,
  },
  { _id: false }
);

const HistorialEstadoSchema = new Schema<IHistorialEstado>(
  {
    estado: { type: String, required: true },
    fecha: { type: Date, default: Date.now },
  },
  { _id: false }
);

const PedidoSchema = new Schema<IPedido>({
  pedidoId: { type: String, required: true, unique: true },
  clienteId: { type: String, required: true },
  clienteEmail: { type: String, required: true },
  clienteNombre: { type: String, required: true },
  clienteDireccion: String,
  fechaCreacion: { type: Date, default: Date.now },
  fechaActualizacion: Date,
  estado: { type: String, required: true, default: 'PENDIENTE' },
  items: [ItemSchema],
  promocionId: String,
  notas: String,
  subtotal: Number,
  descuento: Number,
  total: Number,
  igv: Number,
  totalConIgv: Number,
  facturaId: String,
  transportistaId: String,
  tiempoEstimadoEntregaMin: Number,
  estadoTransporte: String,
  historialEstados: [HistorialEstadoSchema],
});

PedidoSchema.index({ clienteId: 1 });
PedidoSchema.index({ fechaCreacion: -1 });
PedidoSchema.index({ estado: 1 });

export const Pedido = mongoose.model<IPedido>('Pedido', PedidoSchema);

// Contador para IDs secuenciales por día
interface ICounter extends Document {
  _id: string;
  seq: number;
}

const CounterSchema = new Schema<ICounter>({
  _id: { type: String, required: true },
  seq: { type: Number, default: 0 },
});

const Counter = mongoose.model<ICounter>('Counter', CounterSchema);

export async function generarPedidoId(): Promise<string> {
  const date = new Date();
  const dateStr = date.toISOString().slice(0, 10).replace(/-/g, '');
  const key = `pedidos-${dateStr}`;

  const counter = await Counter.findOneAndUpdate(
    { _id: key },
    { $inc: { seq: 1 } },
    { new: true, upsert: true }
  );

  const seq = (counter?.seq ?? 1).toString().padStart(3, '0');
  return `PED-${dateStr}-${seq}`;
}

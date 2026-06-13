import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  ManyToOne,
  JoinColumn,
} from "typeorm";
import { Factura } from "./Factura";

@Entity("items_factura")
export class ItemFactura {
  @PrimaryGeneratedColumn({ type: "integer" })
  id: number;

  @Column({ type: "integer" })
  factura_id: number;

  @Column({ type: "varchar", length: 50 })
  producto_id: string;

  @Column({ type: "varchar", length: 100 })
  nombre_producto: string;

  @Column({ type: "integer" })
  cantidad: number;

  @Column({ type: "decimal", precision: 10, scale: 2 })
  precio_unitario: number;

  @Column({ type: "decimal", precision: 10, scale: 2, default: 0 })
  descuento_aplicado: number;

  @ManyToOne(() => Factura, (factura) => factura.items, { onDelete: "CASCADE" })
  @JoinColumn({ name: "factura_id" })
  factura: Factura;
}

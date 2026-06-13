import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  OneToMany,
} from "typeorm";
import { ItemFactura } from "./ItemFactura";

@Entity("facturas")
export class Factura {
  @PrimaryGeneratedColumn({ type: "integer" })
  id: number;

  @Column({ type: "varchar", length: 50, unique: true })
  factura_id: string;

  @Column({ type: "varchar", length: 50 })
  pedido_id: string;

  @Column({ type: "varchar", length: 50 })
  cliente_id: string;

  @Column({ type: "varchar", length: 200, nullable: true })
  cliente_nombre: string;

  @Column({ type: "varchar", length: 20, nullable: true })
  cliente_ruc: string;

  @Column({ type: "text", nullable: true })
  cliente_direccion: string;

  @Column({ type: "decimal", precision: 10, scale: 2 })
  subtotal: number;

  @Column({ type: "decimal", precision: 10, scale: 2, default: 0 })
  descuento: number;

  @Column({ type: "decimal", precision: 10, scale: 2 })
  total: number;

  @Column({ type: "decimal", precision: 10, scale: 2, default: 0 })
  igv: number;

  @CreateDateColumn({ type: "timestamp" })
  fecha_emision: Date;

  @Column({ type: "varchar", length: 20, default: "EMITIDA" })
  estado: string;

  @Column({ type: "varchar", length: 50, nullable: true })
  promocion_aplicada: string;

  @OneToMany(() => ItemFactura, (item) => item.factura, { cascade: true })
  items: ItemFactura[];
}

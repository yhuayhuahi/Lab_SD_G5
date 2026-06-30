import { inventory } from "../entities/inventory.entity.js"
import { orders, orderId } from "../entities/order.entity.js"

export class OrderController {
  static createOrder(req, res) {
    const { productId, quantity, customerEmail } = req.body;
    
    if (!productId || !quantity || !customerEmail) {
      return res.status(400).json({
        success: false,
        error: 'Faltan campos requeridos: productId, quantity, customerEmail'
      });
    }
    
    const product = inventory.find(item => item.id === productId);
    if (!product) {
      return res.status(404).json({
        success: false,
        error: 'Producto no encontrado'
      });
    }
    
    if (product.stock < quantity) {
      return res.status(400).json({
        success: false,
        error: 'Stock insuficiente'
      });
    }
    
    // Crear el pedido
    const order = {
      id: orderId++,
      productId,
      productName: product.name,
      quantity,
      total: product.price * quantity,
      customerEmail,
      status: 'pendiente',
      createdAt: new Date()
    };
    
    orders.push(order);
    
    // Actualizar stock
    product.stock -= quantity;
    
    res.status(201).json({
      success: true,
      data: order
    });
  }

  static getAll(_, res) {
    res.json({
      success: true,
      count: orders.length,
      data: orders
    })
  }
}

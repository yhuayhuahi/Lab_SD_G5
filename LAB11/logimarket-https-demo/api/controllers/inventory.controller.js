import { inventory } from "../entities/inventory.entity.js"

export class InventoryController {
  static getAll(req, res) {
    const { category } = req.query

    if (category) {
      const filtered = inventory.filter(item => 
        item.category.toLowerCase() === category.toLowerCase()
      )
      
      return res.json({
        success: true,
        count: filtered.length,
        data: filtered
      })
    }
    
    res.json({
      success: true,
      count: inventory.length,
      data: inventory
    })
  }

  static getById(req, res) {
    const id = parseInt(req.params.id)
    const product = inventory.find(item => item.id === id)
    
    if (!product) {
      return res.status(404).json({
        success: false,
        error: 'Producto no encontrado'
      })
    }
    
    res.json({
      success: true,
      data: product
    })
  }
}


import { customers } from "../entities/customer.entity.js";

export class CustomerController {
  static getAll(_, res) {
    res.status(200).json({
      success: true,
      data: customers
    })
  }
}

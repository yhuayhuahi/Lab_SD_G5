package soap;

import java.util.HashMap;
import java.util.Map;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService
public class VentasSOAP {

    // Base de datos simulada en memoria
    private static Map<Integer, String[]> productos = new HashMap<>();
    private static Map<Integer, String[]> pedidos   = new HashMap<>();
    private static int contadorPedidos = 1;

    static {
        // id -> [nombre, precio, stock]
        productos.put(1, new String[]{"Laptop",    "2500.00", "10"});
        productos.put(2, new String[]{"Mouse",     "45.00",   "50"});
        productos.put(3, new String[]{"Teclado",   "120.00",  "30"});
        productos.put(4, new String[]{"Monitor",   "850.00",  "8"});
        productos.put(5, new String[]{"Auriculares","200.00", "20"});
    }

    @WebMethod
    public String consultarProducto(int id) {
        String[] p = productos.get(id);
        if (p == null) return "ERROR: Producto no encontrado";
        return String.format(
            "ID:%d | %s | S/ %s | Stock: %s unidades", id, p[0], p[1], p[2]
        );
    }

    @WebMethod
    public String listarProductos() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String[]> e : productos.entrySet()) {
            String[] p = e.getValue();
            sb.append(String.format(
                "ID:%d | %s | S/ %s | Stock: %s%n",
                e.getKey(), p[0], p[1], p[2]
            ));
        }
        return sb.toString();
    }

    @WebMethod
    public String realizarPedido(int productoId, int cantidad) {
        String[] p = productos.get(productoId); // obtener producto por id
        if (p == null)  return "ERROR: Producto no encontrado";

        int stock = Integer.parseInt(p[2]);
        if (cantidad <= 0)    return "ERROR: Cantidad invalida";
        if (cantidad > stock) return "ERROR: Stock insuficiente (disponible: " + stock + ")";

        // Descontar stock y registrar pedido
        p[2] = String.valueOf(stock - cantidad); 
        double total = Double.parseDouble(p[1]) * cantidad;
        int pedidoId = contadorPedidos++;
        pedidos.put(pedidoId, new String[]{
            p[0], String.valueOf(cantidad), String.format("%.2f", total), "CONFIRMADO"
        });

        return String.format(
            "PEDIDO #%d confirmado | %s x%d | Total: S/ %.2f",
            pedidoId, p[0], cantidad, total
        );
    }

    @WebMethod
    public String consultarPedido(int pedidoId) {
        String[] ped = pedidos.get(pedidoId);
        if (ped == null) return "ERROR: Pedido no encontrado";
        return String.format(
            "Pedido #%d | %s x%s | Total: S/ %s | Estado: %s",
            pedidoId, ped[0], ped[1], ped[2], ped[3]
        );
    }
}
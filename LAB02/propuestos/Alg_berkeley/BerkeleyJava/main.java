import java.util.ArrayList;
import java.util.List;

public class main {
    public static void main(String[] args) {

        try {
            List<Cliente> clientes = new ArrayList<>();

            clientes.add(new Cliente("Cliente 1", 93));
            clientes.add(new Cliente("Cliente 2", 105));
            clientes.add(new Cliente("Cliente 3", 98));
            clientes.add(new Cliente("Cliente 4", 110));

            // Paso 1: elección de maestro
            double tiempoServidor = 100;
            System.out.println("Nodo maestro elegido con tiempo: " + tiempoServidor);

            System.out.println("\n=== Antes de sincronizar ===");
            for (Cliente c : clientes) {
                c.mostrarTiempo();
            }

            Servidor servidor = new Servidor(clientes, tiempoServidor); // se crea servidor con arreglo de clientes y su tiempo
            servidor.sincronizar();

            System.out.println("\nDespués de sincronizar...");

            System.out.println("Servidor -> " + servidor.getTiempoServidor());
            for (Cliente c : clientes) {
                c.mostrarTiempo();
            }

        } catch (Exception e) {
            System.out.println("Error general: " + e.getMessage());
        }
    }
}
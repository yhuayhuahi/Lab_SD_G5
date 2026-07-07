import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Server {

    public static void main(String[] args) {

        try {

            // Crear registro RMI en el puerto 1099
            LocateRegistry.createRegistry(1099);

            // Crear objeto remoto
            CalculatorImpl calculator = new CalculatorImpl();

            // Registrar servicio
            Naming.rebind("CalculatorService", calculator);

            System.out.println("Servidor RPC ejecutándose...");
            System.out.println("Servicio registrado correctamente");

        } catch (Exception e) {
            System.out.println("Error en el servidor:");
            e.printStackTrace();
        }
    }
}
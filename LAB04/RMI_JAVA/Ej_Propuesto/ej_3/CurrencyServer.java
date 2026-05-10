import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CurrencyServer {
    public static void main(String[] args) {
        try {
            // Crear objeto remoto
            CurrencyConverter converter = new CurrencyConverterImpl();

            // Iniciar registro RMI en el puerto 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            // Registrar el objeto remoto con nombre "CurrencyConverter"
            registry.rebind("CurrencyConverter", converter);

            System.out.println("Servidor RMI de conversión iniciado...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
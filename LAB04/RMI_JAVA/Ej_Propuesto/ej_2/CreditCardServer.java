import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CreditCardServer {
    public static void main(String[] args) {
        try {
            // Crear objeto remoto
            CreditCardService service = new CreditCardServiceImpl();
            
            // El servidor mismo crea el registro (no necesita rmiregistry externo)
            Registry registry = LocateRegistry.createRegistry(1099);
            
            // Registrar el objeto remoto
            registry.rebind("CreditCardService", service);
            
            System.out.println("Servidor de Tarjetas de Crédito iniciado...");
            System.out.println("Servidor listo para aceptar conexiones");
            System.out.println("Registro RMI creado en puerto 1099");
            
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
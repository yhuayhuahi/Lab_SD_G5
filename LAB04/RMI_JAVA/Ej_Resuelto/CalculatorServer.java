import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CalculatorServer {

    public CalculatorServer() {
        try {
            Calculator c = new CalculatorImpl();
            
            // Iniciamos el registro RMI en el puerto 1099 
            // (Esto evita tener que ejecutar 'rmiregistry' manualmente en la consola)
            LocateRegistry.createRegistry(1099);
            
            Naming.rebind("rmi://localhost:1099/CalculatorService", c); // registra el servicio remoto
            System.out.println("Servidor RMI Calculadora iniciado y esperando conexiones...");
            
        } catch (Exception e) { // maneja cualquier excepcion
            System.out.println("Trouble: " + e);
        }
    }

    public static void main(String args[]) {
        new CalculatorServer();
    }
}
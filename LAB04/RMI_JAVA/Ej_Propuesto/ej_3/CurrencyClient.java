import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CurrencyClient {
    public static void main(String[] args) {
        if(args.length < 2){
            System.out.println("Uso: java CurrencyClient <monto> <moneda>");
            System.out.println("Moneda: USD o EUR");
            return;
        }

        try {
            double monto = Double.parseDouble(args[0]);
            String moneda = args[1].toUpperCase();

            // Conectar al registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost");
            CurrencyConverter converter = (CurrencyConverter) registry.lookup("CurrencyConverter");

            // Llamada remota según moneda
            if(moneda.equals("USD")){
                double resultado = converter.convertirADolares(monto);
                System.out.println(monto + " soles = " + resultado + " USD");
            } else if(moneda.equals("EUR")){
                double resultado = converter.convertirAEuros(monto);
                System.out.println(monto + " soles = " + resultado + " EUR");
            } else {
                System.out.println("Moneda desconocida. Use USD o EUR.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
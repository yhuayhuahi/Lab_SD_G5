import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public class CalculatorClient {

    public static void main(String[] args) { // verifica que se pasen dos argumentos 
        if (args.length < 2) {
            System.out.println("Uso: java CalculatorClient <num1> <num2>");
            return;
        }

        int num1 = Integer.parseInt(args[0]);
        int num2 = Integer.parseInt(args[1]);

        try {
            Calculator c = (Calculator) Naming.lookup("rmi://localhost:1099/CalculatorService"); // busca el servicio remoto
            
            System.out.println("The substraction of " + num1 + " and " + num2 + " is: " + c.sub(num1, num2));
            System.out.println("The addition of " + num1 + " and " + num2 + " is: " + c.add(num1, num2));
            System.out.println("The multiplication of " + num1 + " and " + num2 + " is: " + c.mul(num1, num2));
            System.out.println("The division of " + num1 + " and " + num2 + " is: " + c.div(num1, num2));
            
        } catch (MalformedURLException murle) { // maneja excepciones específicas de RMI
            System.out.println("\nMalformedURLException: " + murle);
        } catch (RemoteException re) {
            System.out.println("\nRemoteException: " + re);
        } catch (NotBoundException nbe) {
            System.out.println("\nNotBoundException: " + nbe);
        } catch (java.lang.ArithmeticException ae) {
            System.out.println("\njava.lang.ArithmeticException: " + ae);
        }
    }
}
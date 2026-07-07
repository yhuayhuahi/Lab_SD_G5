import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    // Constructor obligatorio
    protected CalculatorImpl() throws RemoteException {
        super();
    }

    // Multiplicación
    @Override
    public double multiply(double a, double b) throws RemoteException {
        return a * b;
    }

    // División
    @Override
    public double divide(double a, double b) throws RemoteException {

        if (b == 0) {
            throw new ArithmeticException("No se puede dividir entre cero");
        }

        return a / b;
    }

    // Potencia
    @Override
    public double power(double a, double b) throws RemoteException {
        return Math.pow(a, b);
    }
}
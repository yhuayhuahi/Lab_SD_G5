import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CreditCardService extends Remote {
    // Consultar saldo disponible
    double consultarSaldo(String numeroTarjeta) throws RemoteException;
    
    // Realizar un pago
    boolean realizarPago(String numeroTarjeta, double monto, String concepto) throws RemoteException;
    
    // Consultar última transacción
    String ultimaTransaccion(String numeroTarjeta) throws RemoteException;
    
    // Pagar factura (reducir deuda)
    boolean pagarFactura(String numeroTarjeta, double monto) throws RemoteException;
}

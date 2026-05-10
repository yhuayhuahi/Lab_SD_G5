import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CurrencyConverterImpl extends UnicastRemoteObject implements CurrencyConverter {

    private static final long serialVersionUID = 1L;
    
    // Tasas fijas de conversión
    private static final double TASA_DOLAR = 0.27; // 1 sol = 0.27 USD
    private static final double TASA_EURO = 0.25;  // 1 sol = 0.25 EUR

    protected CurrencyConverterImpl() throws RemoteException {
        super();
    }

    @Override
    public double convertirADolares(double monto) throws RemoteException {
        return monto * TASA_DOLAR;
    }

    @Override
    public double convertirAEuros(double monto) throws RemoteException {
        return monto * TASA_EURO;
    }
}
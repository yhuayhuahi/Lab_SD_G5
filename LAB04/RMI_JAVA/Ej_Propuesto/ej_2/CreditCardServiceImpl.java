import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class CreditCardServiceImpl extends UnicastRemoteObject implements CreditCardService {
    
    private static final long serialVersionUID = 1L;
    
    // Base de datos simulada de tarjetas
    private Map<String, TarjetaCredito> tarjetas;
    
    protected CreditCardServiceImpl() throws RemoteException {
        super();
        inicializarTarjetas();
    }
    
    private void inicializarTarjetas() {
        tarjetas = new HashMap<>();
        tarjetas.put("1111-2222-3333-4444", new TarjetaCredito("1111-2222-3333-4444", 5000.0, 0.0));
        tarjetas.put("5555-6666-7777-8888", new TarjetaCredito("5555-6666-7777-8888", 3000.0, 0.0));
        tarjetas.put("9999-0000-1111-2222", new TarjetaCredito("9999-0000-1111-2222", 10000.0, 0.0));
    }
    
    @Override
    public double consultarSaldo(String numeroTarjeta) throws RemoteException {
        TarjetaCredito tarjeta = tarjetas.get(numeroTarjeta);
        if (tarjeta == null) {
            throw new RemoteException("Tarjeta no encontrada");
        }
        return tarjeta.getSaldoDisponible();
    }
    
    @Override
    public boolean realizarPago(String numeroTarjeta, double monto, String concepto) throws RemoteException {
        TarjetaCredito tarjeta = tarjetas.get(numeroTarjeta);
        if (tarjeta == null) {
            throw new RemoteException("Tarjeta no encontrada");
        }
        
        if (monto <= 0) {
            throw new RemoteException("El monto debe ser positivo");
        }
        
        if (tarjeta.getSaldoDisponible() >= monto) {
            tarjeta.setSaldoDisponible(tarjeta.getSaldoDisponible() - monto);
            tarjeta.setDeudaActual(tarjeta.getDeudaActual() + monto);
            tarjeta.setUltimaTransaccion("Pago de " + monto + " soles - Concepto: " + concepto);
            return true;
        }
        return false;
    }
    
    @Override
    public String ultimaTransaccion(String numeroTarjeta) throws RemoteException {
        TarjetaCredito tarjeta = tarjetas.get(numeroTarjeta);
        if (tarjeta == null) {
            throw new RemoteException("Tarjeta no encontrada");
        }
        return tarjeta.getUltimaTransaccion();
    }
    
    @Override
    public boolean pagarFactura(String numeroTarjeta, double monto) throws RemoteException {
        TarjetaCredito tarjeta = tarjetas.get(numeroTarjeta);
        if (tarjeta == null) {
            throw new RemoteException("Tarjeta no encontrada");
        }
        
        if (monto <= 0) {
            throw new RemoteException("El monto debe ser positivo");
        }
        
        if (tarjeta.getDeudaActual() >= monto) {
            tarjeta.setDeudaActual(tarjeta.getDeudaActual() - monto);
            tarjeta.setSaldoDisponible(tarjeta.getSaldoDisponible() + monto);
            tarjeta.setUltimaTransaccion("Pago de factura de " + monto + " soles");
            return true;
        } else if (tarjeta.getDeudaActual() > 0) {
            // Pago parcial
            tarjeta.setDeudaActual(0);
            double sobrante = monto - tarjeta.getDeudaActual();
            tarjeta.setSaldoDisponible(tarjeta.getSaldoDisponible() + tarjeta.getDeudaActual());
            tarjeta.setUltimaTransaccion("Pago total de deuda. Sobrante: " + sobrante);
            return true;
        }
        return false;
    }
    
    // Clase interna para representar una tarjeta
    private class TarjetaCredito implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private String numero;
        private double saldoDisponible;
        private double deudaActual;
        private String ultimaTransaccion;
        
        public TarjetaCredito(String numero, double saldoDisponible, double deudaActual) {
            this.numero = numero;
            this.saldoDisponible = saldoDisponible;
            this.deudaActual = deudaActual;
            this.ultimaTransaccion = "Ninguna transacción aún";
        }
        
        public String getNumero() { return numero; }
        public double getSaldoDisponible() { return saldoDisponible; }
        public void setSaldoDisponible(double saldoDisponible) { this.saldoDisponible = saldoDisponible; }
        public double getDeudaActual() { return deudaActual; }
        public void setDeudaActual(double deudaActual) { this.deudaActual = deudaActual; }
        public String getUltimaTransaccion() { return ultimaTransaccion; }
        public void setUltimaTransaccion(String ultimaTransaccion) { this.ultimaTransaccion = ultimaTransaccion; }
    }
}

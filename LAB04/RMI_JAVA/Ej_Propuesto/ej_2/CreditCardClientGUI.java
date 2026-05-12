import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CreditCardClientGUI {
    private CreditCardService service;
    private JFrame frame;
    private JTextField tarjetaField;
    private JTextArea resultadoArea;
    
    public CreditCardClientGUI() {
        conectarServidor();
        crearGUI();
    }
    
    private void conectarServidor() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            service = (CreditCardService) registry.lookup("CreditCardService");
            System.out.println("Conectado al servidor RMI");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al conectar con el servidor: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void crearGUI() {
        frame = new JFrame("Sistema de Tarjetas de Crédito");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        
        // Panel superior para número de tarjeta
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Número de Tarjeta:"));
        tarjetaField = new JTextField(20);
        topPanel.add(tarjetaField);
        frame.add(topPanel, BorderLayout.NORTH);
        
        // Panel central para botones
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton consultarBtn = new JButton("Consultar Saldo");
        JButton pagarBtn = new JButton("Realizar Pago");
        JButton facturaBtn = new JButton("Pagar Factura");
        JButton transaccionBtn = new JButton("Última Transacción");
        
        centerPanel.add(consultarBtn);
        centerPanel.add(pagarBtn);
        centerPanel.add(facturaBtn);
        centerPanel.add(transaccionBtn);
        
        frame.add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior para resultados
        resultadoArea = new JTextArea(10, 40);
        resultadoArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultadoArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));
        frame.add(scrollPane, BorderLayout.SOUTH);
        
        // Acciones de los botones
        consultarBtn.addActionListener(e -> consultarSaldo());
        pagarBtn.addActionListener(e -> realizarPago());
        facturaBtn.addActionListener(e -> pagarFactura());
        transaccionBtn.addActionListener(e -> ultimaTransaccion());
        
        frame.setVisible(true);
    }
    
    private void consultarSaldo() {
        try {
            String tarjeta = tarjetaField.getText().trim();
            if (tarjeta.isEmpty()) {
                mostrarError("Ingrese un número de tarjeta");
                return;
            }
            
            double saldo = service.consultarSaldo(tarjeta);
            resultadoArea.append("Saldo disponible: " + saldo + " soles\n");
            
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }
    
    private void realizarPago() {
        try {
            String tarjeta = tarjetaField.getText().trim();
            if (tarjeta.isEmpty()) {
                mostrarError("Ingrese un número de tarjeta");
                return;
            }
            
            String montoStr = JOptionPane.showInputDialog(frame, "Ingrese el monto a pagar:");
            if (montoStr == null) return;
            
            double monto = Double.parseDouble(montoStr);
            
            String concepto = JOptionPane.showInputDialog(frame, "Ingrese el concepto del pago:");
            if (concepto == null) return;
            
            boolean exito = service.realizarPago(tarjeta, monto, concepto);
            if (exito) {
                resultadoArea.append("Pago realizado con éxito: " + monto + " soles - " + concepto + "\n");
                // Mostrar nuevo saldo
                double nuevoSaldo = service.consultarSaldo(tarjeta);
                resultadoArea.append("Nuevo saldo disponible: " + nuevoSaldo + " soles\n");
            } else {
                resultadoArea.append("Error: Saldo insuficiente\n");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un monto válido");
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }
    
    private void pagarFactura() {
        try {
            String tarjeta = tarjetaField.getText().trim();
            if (tarjeta.isEmpty()) {
                mostrarError("Ingrese un número de tarjeta");
                return;
            }
            
            String montoStr = JOptionPane.showInputDialog(frame, "Ingrese el monto a pagar de la factura:");
            if (montoStr == null) return;
            
            double monto = Double.parseDouble(montoStr);
            
            boolean exito = service.pagarFactura(tarjeta, monto);
            if (exito) {
                resultadoArea.append("Pago de factura realizado con éxito: " + monto + " soles\n");
                double nuevoSaldo = service.consultarSaldo(tarjeta);
                resultadoArea.append("Nuevo saldo disponible: " + nuevoSaldo + " soles\n");
            } else {
                resultadoArea.append("Error: No se pudo procesar el pago\n");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un monto válido");
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }
    
    private void ultimaTransaccion() {
        try {
            String tarjeta = tarjetaField.getText().trim();
            if (tarjeta.isEmpty()) {
                mostrarError("Ingrese un número de tarjeta");
                return;
            }
            
            String transaccion = service.ultimaTransaccion(tarjeta);
            resultadoArea.append("Última transacción: " + transaccion + "\n");
            
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }
    
    private void mostrarError(String mensaje) {
        resultadoArea.append("ERROR: " + mensaje + "\n");
        JOptionPane.showMessageDialog(frame, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CreditCardClientGUI());
    }
}
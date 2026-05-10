import javax.swing.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class CurrencyClientGUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Conversor de Moneda");
        frame.setSize(300, 220);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Campo para monto
        JTextField montoField = new JTextField();
        montoField.setBounds(120, 20, 150, 25);
        JLabel lblMonto = new JLabel("Monto en soles:");
        lblMonto.setBounds(10, 20, 100, 25);

        // Combo para moneda
        String[] monedas = {"USD", "EUR"};
        JComboBox<String> monedaBox = new JComboBox<>(monedas);
        monedaBox.setBounds(120, 60, 150, 25);

        // Botón de conversión
        JButton convertirBtn = new JButton("Convertir");
        convertirBtn.setBounds(120, 100, 150, 25);

        // Etiqueta para mostrar resultado
        JLabel resultadoLbl = new JLabel("Resultado:");
        resultadoLbl.setBounds(10, 140, 250, 25);

        frame.setLayout(null);
        frame.add(lblMonto);
        frame.add(montoField);
        frame.add(monedaBox);
        frame.add(convertirBtn);
        frame.add(resultadoLbl);

        // Acción del botón
        convertirBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    double monto = Double.parseDouble(montoField.getText());
                    String moneda = monedaBox.getSelectedItem().toString();

                    // Conexión RMI
                    Registry registry = LocateRegistry.getRegistry("localhost");
                    CurrencyConverter converter = (CurrencyConverter) registry.lookup("CurrencyConverter");

                    double resultado;
                    if(moneda.equals("USD")) {
                        resultado = converter.convertirADolares(monto);
                    } else {
                        resultado = converter.convertirAEuros(monto);
                    }

                    resultadoLbl.setText("Resultado: " + resultado + " " + moneda);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Ingrese un número válido.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        frame.setVisible(true);
    }
}
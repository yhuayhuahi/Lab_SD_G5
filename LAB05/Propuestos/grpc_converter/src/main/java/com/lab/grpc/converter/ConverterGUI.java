package com.lab.grpc.converter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.lab.grpc.converter.ConverterGrpc;
import com.lab.grpc.converter.ConverterProto;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ConverterGUI extends JFrame {

    private final ManagedChannel channel;
    private final ConverterGrpc.ConverterBlockingStub stub;

    private JComboBox<String> tipoCombo;
    private JTextField valorField;
    private JTextArea resultArea;
    private JLabel statusLabel;

    public ConverterGUI() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = ConverterGrpc.newBlockingStub(channel);
        initUI();
    }

    private void initUI() {
        setTitle("Sistema de Conversión gRPC - Lab 05 UNSA 2026");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(580, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.setBackground(new Color(245, 245, 250));

        // Título
        JLabel titulo = new JLabel("Sistema de Conversión gRPC", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(33, 97, 140));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        main.add(titulo);

        // Panel entrada
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(33, 97, 140)), "Parámetros de Conversión"));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("Tipo de conversión:"), gbc);

        tipoCombo = new JComboBox<>(new String[]{
            "celsius_fahrenheit",
            "fahrenheit_celsius",
            "celsius_kelvin",
            "kelvin_celsius",
            "fahrenheit_kelvin",
            "kelvin_fahrenheit",
            "soles_dolares",
            "dolares_soles",
            "km_millas",
            "millas_km",
            "kg_g",
            "g_kg",
            "kg_lb",
            "lb_kg",
            "mg_g",
            "g_mg"
        });
        tipoCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(tipoCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        inputPanel.add(new JLabel("Valor a convertir:"), gbc);

        valorField = new JTextField("100.0");
        valorField.setFont(new Font("Arial", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputPanel.add(valorField, gbc);

        JButton btnConvertir = new JButton("  Convertir  ");
        btnConvertir.setBackground(new Color(33, 97, 140));
        btnConvertir.setForeground(Color.BLACK);
        btnConvertir.setFont(new Font("Arial", Font.BOLD, 14));
        btnConvertir.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        inputPanel.add(btnConvertir, gbc);

        main.add(inputPanel);
        main.add(Box.createVerticalStrut(10));

        // Panel resultados
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(33, 97, 140)), "Resultados"));

        resultArea = new JTextArea(12, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBackground(new Color(250, 250, 250));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> resultArea.setText(""));
        resultPanel.add(btnLimpiar, BorderLayout.SOUTH);

        main.add(resultPanel);
        main.add(Box.createVerticalStrut(8));

        // Status
        statusLabel = new JLabel("Conectado al servidor gRPC en localhost:50051");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(39, 174, 96));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(statusLabel);

        add(main);

        btnConvertir.addActionListener(e -> convertir());
        valorField.addActionListener(e -> convertir());
    }

    private void convertir() {
        String tipo = (String) tipoCombo.getSelectedItem();
        double valor;
        try {
            valor = Double.parseDouble(valorField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ConverterProto.ConvertRequest req = ConverterProto.ConvertRequest.newBuilder()
                .setType(tipo).setValue(valor).build();

        long inicio = System.currentTimeMillis();
        try {
            ConverterProto.ConvertResponse resp = stub.convert(req);
            long ms = System.currentTimeMillis() - inicio;
            if (resp.getSuccess()) {
                resultArea.append("✓ " + resp.getDescription() + "\n");
                resultArea.append("  Resultado: " + String.format("%.6f", resp.getResult())
                        + "  |  Tiempo: " + ms + " ms\n");
                resultArea.append("─────────────────────────────────\n");
                statusLabel.setText("OK - última consulta: " + ms + " ms");
                statusLabel.setForeground(new Color(39, 174, 96));
            } else {
                resultArea.append("✗ ERROR: " + resp.getErrorMessage() + "\n");
                resultArea.append("─────────────────────────────────\n");
                statusLabel.setText("Error en la conversión");
                statusLabel.setForeground(Color.RED);
            }
        } catch (StatusRuntimeException ex) {
            resultArea.append("✗ ERROR DE RED: " + ex.getStatus() + "\n");
            statusLabel.setText("Sin conexión con el servidor");
            statusLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new ConverterGUI().setVisible(true));
    }
}

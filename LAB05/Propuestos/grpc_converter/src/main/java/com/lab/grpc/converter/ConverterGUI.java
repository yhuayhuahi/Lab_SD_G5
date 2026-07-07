package com.lab.grpc.converter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class ConverterGUI extends JFrame {

    private static final Color BLUE = new Color(51, 102, 153);
    private static final Color DARK_BLUE = new Color(0, 0, 102);
    private static final Color LIGHT_ROW = new Color(229, 242, 255);
    private static final Color PAGE_BACKGROUND = new Color(221, 221, 221);

    private final ManagedChannel channel;
    private final ConverterGrpc.ConverterBlockingStub stub;

    private JComboBox<CategoryOption> categoryCombo;
    private JComboBox<UnitOption> fromUnitCombo;
    private JComboBox<UnitOption> toUnitCombo;
    private JTextField valueField;
    private JTextField resultField;
    private JTextArea historyArea;
    private JLabel statusLabel;

    public ConverterGUI() {
        channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = ConverterGrpc.newBlockingStub(channel);

        initUI();
        loadCatalog();
    }

    private void initUI() {
        setTitle("Conversor de Unidades gRPC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 610);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(PAGE_BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        page.add(createHeader());
        page.add(Box.createVerticalStrut(12));
        page.add(createMenuPanel());
        page.add(Box.createVerticalStrut(12));
        page.add(createConverterPanel());
        page.add(Box.createVerticalStrut(12));
        page.add(createHistoryPanel());
        page.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel("Conectando con servidor gRPC en localhost:50051");
        statusLabel.setFont(new Font("Verdana", Font.ITALIC, 11));
        statusLabel.setForeground(BLUE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(statusLabel);

        add(page);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BLUE);
        header.setPreferredSize(new Dimension(820, 85));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("Conversor de Unidades .gRPC");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Verdana", Font.BOLD, 23));

        JLabel subtitle = new JLabel("convertir medidas usando un servicio distribuido con Protocol Buffers");
        subtitle.setForeground(new Color(230, 230, 230));
        subtitle.setFont(new Font("Verdana", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(subtitle);

        header.add(text, BorderLayout.WEST);
        return header;
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 249));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        String[] labels = {
                "Area", "Fuerza", "Temperatura", "Longitud", "Masa",
                "Potencia", "Volumen", "Energia", "Presion", "Moneda",
                "Tiempo", "Velocidad", "Densidad", "Caudal"
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 14, 4, 14);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = i % 7;
            gbc.gridy = i / 7;

            JLabel label = new JLabel(labels[i]);
            label.setForeground(BLUE);
            label.setFont(new Font("Verdana", Font.BOLD, 12));
            panel.add(label, gbc);
        }

        return panel;
    }

    private JPanel createConverterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 203, 255)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel section = new JLabel("Conversor de Unidades Online", SwingConstants.CENTER);
        section.setForeground(BLUE);
        section.setFont(new Font("Verdana", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        panel.add(section, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        categoryCombo.addActionListener(e -> updateUnitCombos());

        gbc.gridx = 0;
        gbc.weightx = 0.7;
        panel.add(categoryCombo, gbc);

        valueField = new JTextField("100");
        valueField.setFont(new Font("Verdana", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(valueField, gbc);

        fromUnitCombo = new JComboBox<>();
        fromUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        panel.add(fromUnitCombo, gbc);

        JButton swapButton = new JButton(">");
        swapButton.setToolTipText("Intercambiar unidades");
        swapButton.addActionListener(e -> swapUnits());
        gbc.gridx = 3;
        gbc.weightx = 0;
        panel.add(swapButton, gbc);

        toUnitCombo = new JComboBox<>();
        toUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        gbc.gridx = 4;
        gbc.weightx = 1.0;
        panel.add(toUnitCombo, gbc);

        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setFont(new Font("Verdana", Font.BOLD, 13));
        resultField.setBackground(new Color(250, 250, 250));

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel.add(resultField, gbc);

        JButton convertButton = new JButton("Convertir");
        convertButton.setBackground(BLUE);
        convertButton.setForeground(Color.BLACK);
        convertButton.setFont(new Font("Verdana", Font.BOLD, 13));
        convertButton.addActionListener(e -> convert());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(convertButton, gbc);

        valueField.addActionListener(e -> convert());

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BLUE),
                "Historial de conversiones"
        ));

        historyArea = new JTextArea(12, 65);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        historyArea.setBackground(new Color(250, 250, 250));

        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setBackground(Color.WHITE);

        JButton clearButton = new JButton("Limpiar historial");
        clearButton.addActionListener(e -> historyArea.setText(""));

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetForm());

        buttons.add(clearButton);
        buttons.add(resetButton);

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadCatalog() {
        try {
            CatalogResponse catalog = stub.getCatalog(CatalogRequest.newBuilder().build());

            categoryCombo.removeAllItems();
            for (CategoryInfo category : catalog.getCategoriesList()) {
                categoryCombo.addItem(new CategoryOption(category));
            }

            statusLabel.setText("Catalogo cargado desde el servidor gRPC");
            statusLabel.setForeground(new Color(39, 174, 96));

            updateUnitCombos();
        } catch (StatusRuntimeException ex) {
            statusLabel.setText("No se pudo conectar con el servidor gRPC");
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Primero inicia el servidor gRPC.",
                    "Servidor no disponible",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUnitCombos() {
        CategoryOption selected = (CategoryOption) categoryCombo.getSelectedItem();
        if (selected == null) {
            return;
        }

        CategoryInfo category = selected.category();

        fromUnitCombo.removeAllItems();
        toUnitCombo.removeAllItems();

        for (UnitInfo unit : category.getUnitsList()) {
            fromUnitCombo.addItem(new UnitOption(unit));
            toUnitCombo.addItem(new UnitOption(unit));
        }

        selectUnit(fromUnitCombo, category.getDefaultFromUnit());
        selectUnit(toUnitCombo, category.getDefaultToUnit());
    }

    private void selectUnit(JComboBox<UnitOption> combo, String code) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            UnitOption item = combo.getItemAt(i);
            if (item.unit().getCode().equalsIgnoreCase(code)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void swapUnits() {
        int fromIndex = fromUnitCombo.getSelectedIndex();
        int toIndex = toUnitCombo.getSelectedIndex();

        fromUnitCombo.setSelectedIndex(toIndex);
        toUnitCombo.setSelectedIndex(fromIndex);
    }

    private void resetForm() {
        valueField.setText("100");
        resultField.setText("");
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        historyArea.setText("");
        statusLabel.setText("Formulario reiniciado");
        statusLabel.setForeground(BLUE);
    }

    private void convert() {
        CategoryOption categoryOption = (CategoryOption) categoryCombo.getSelectedItem();
        UnitOption fromOption = (UnitOption) fromUnitCombo.getSelectedItem();
        UnitOption toOption = (UnitOption) toUnitCombo.getSelectedItem();

        if (categoryOption == null || fromOption == null || toOption == null) {
            JOptionPane.showMessageDialog(this, "Selecciona categoria y unidades.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un numero valido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ConvertRequest request = ConvertRequest.newBuilder()
                .setCategory(categoryOption.category().getCode())
                .setFromUnit(fromOption.unit().getCode())
                .setToUnit(toOption.unit().getCode())
                .setValue(value)
                .build();

        long start = System.nanoTime();

        try {
            ConvertResponse response = stub.convert(request);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            if (response.getSuccess()) {
                String result = String.format("%.6f", response.getResult());
                resultField.setText(result + " " + toOption.unit().getCode());

                historyArea.append("[OK] " + response.getDescription() + " | " + elapsedMs + " ms\n");
                statusLabel.setText("Conversion realizada en " + elapsedMs + " ms");
                statusLabel.setForeground(new Color(39, 174, 96));
            } else {
                resultField.setText("");
                historyArea.append("[ERROR] " + response.getErrorMessage() + "\n");
                statusLabel.setText("Error en la conversion");
                statusLabel.setForeground(Color.RED);
            }
        } catch (StatusRuntimeException ex) {
            historyArea.append("[ERROR DE RED] " + ex.getStatus() + "\n");
            statusLabel.setText("Sin conexion con el servidor");
            statusLabel.setForeground(Color.RED);
        }
    }

    @Override
    public void dispose() {
        channel.shutdown();
        super.dispose();
    }

    private record CategoryOption(CategoryInfo category) {
        @Override
        public String toString() {
            return category.getLabel();
        }
    }

    private record UnitOption(UnitInfo unit) {
        @Override
        public String toString() {
            return unit.getLabel();
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new ConverterGUI().setVisible(true));
    }
}
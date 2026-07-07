package com.lab.grpc.converter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConverterGUI extends JFrame {

    private static final Color BLUE = new Color(51, 102, 153);
    private static final Color DARK_BLUE = new Color(0, 0, 102);
    private static final Color PAGE_BACKGROUND = new Color(221, 221, 221);
    private static final Color PANEL_BACKGROUND = new Color(245, 247, 249);
    private static final Color ACTIVE_LINK_BG = new Color(229, 242, 255);

    private final ManagedChannel channel;
    private final ConverterGrpc.ConverterBlockingStub stub;

    private final List<CategoryInfo> loadedCategories = new ArrayList<>();
    private final Map<String, JButton> categoryLinkButtons = new LinkedHashMap<>();

    private JPanel menuPanel;
    private JComboBox<CategoryOption> categoryCombo;
    private JComboBox<UnitOption> fromUnitCombo;
    private JComboBox<UnitOption> toUnitCombo;
    private JTextField valueField;
    private JTextField resultField;
    private JTextArea historyArea;
    private JEditorPane symbolPane;
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
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BACKGROUND);

        root.add(createHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PAGE_BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 12, 14));

        content.add(createMenuPanel());
        content.add(Box.createVerticalStrut(14));
        content.add(createConverterPanel());
        content.add(Box.createVerticalStrut(14));
        content.add(createHistoryPanel());
        content.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel("Conectando con servidor gRPC en localhost:50051");
        statusLabel.setFont(new Font("Verdana", Font.ITALIC, 11));
        statusLabel.setForeground(BLUE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(statusLabel);

        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
    }
    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BLUE);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        wrapper.setPreferredSize(new Dimension(1040, 86));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Conversor de Unidades .gRPC");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Verdana", Font.BOLD, 23));

        JLabel subtitle = new JLabel("convertir medidas con un servicio distribuido basado en Protocol Buffers");
        subtitle.setForeground(new Color(225, 225, 225));
        subtitle.setFont(new Font("Verdana", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(subtitle);

        inner.add(text, BorderLayout.WEST);
        wrapper.add(inner, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createMenuPanel() {
        menuPanel = new JPanel(new GridLayout(0, 7, 10, 8));
        menuPanel.setBackground(PANEL_BACKGROUND);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        menuPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return menuPanel;
    }

    private void refreshMenuLinks() {
        menuPanel.removeAll();
        categoryLinkButtons.clear();

        for (CategoryInfo category : loadedCategories) {
            JButton linkButton = createCategoryLinkButton(category);
            categoryLinkButtons.put(category.getCode(), linkButton);
            menuPanel.add(linkButton);
        }

        syncCategoryLinks();
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private JButton createCategoryLinkButton(CategoryInfo category) {
        JButton button = new JButton(category.getLabel());
        button.setFont(new Font("Verdana", Font.BOLD, 12));
        button.setForeground(BLUE);
        button.setBackground(PANEL_BACKGROUND);
        button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> selectCategory(category.getCode()));
        return button;
    }

    private JPanel createConverterPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(PAGE_BACKGROUND);
        wrapper.setPreferredSize(new Dimension(1000, 230));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 241, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 203, 255)),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel section = new JLabel("Conversor de Unidades Online", SwingConstants.CENTER);
        section.setForeground(BLUE);
        section.setFont(new Font("Verdana", Font.BOLD, 18));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        panel.add(section, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;

        JLabel categoryLabel = createFormLabel("Categoria");
        JLabel valueLabel = createFormLabel("Valor a convertir");
        JLabel fromLabel = createFormLabel("Unidad origen");
        JLabel swapLabel = createFormLabel("");
        JLabel resultLabel = createFormLabel("Resultado");
        JLabel toLabel = createFormLabel("Unidad destino");

        gbc.gridx = 0;
        panel.add(categoryLabel, gbc);

        gbc.gridx = 1;
        panel.add(valueLabel, gbc);

        gbc.gridx = 2;
        panel.add(fromLabel, gbc);

        gbc.gridx = 3;
        panel.add(swapLabel, gbc);

        gbc.gridx = 4;
        panel.add(resultLabel, gbc);

        gbc.gridx = 5;
        panel.add(toLabel, gbc);

        gbc.gridy = 2;

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        categoryCombo.setPreferredSize(new Dimension(160, 30));
        categoryCombo.setPrototypeDisplayValue(new CategoryOption(CategoryInfo.newBuilder()
                .setCode("categoria")
                .setLabel("Temperatura")
                .build()));
        categoryCombo.addActionListener(e -> {
            updateUnitCombos();
            syncCategoryLinks();
            clearResultOnly();
        });

        gbc.gridx = 0;
        gbc.weightx = 0.18;
        panel.add(categoryCombo, gbc);

        valueField = new JTextField("100");
        valueField.setFont(new Font("Verdana", Font.PLAIN, 13));
        valueField.setPreferredSize(new Dimension(150, 30));

        gbc.gridx = 1;
        gbc.weightx = 0.14;
        panel.add(valueField, gbc);

        fromUnitCombo = new JComboBox<>();
        fromUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        fromUnitCombo.setPreferredSize(new Dimension(250, 30));
        fromUnitCombo.setPrototypeDisplayValue(new UnitOption(UnitInfo.newBuilder()
                .setCode("kg_m3")
                .setLabel("Kilogramo por metro cubico")
                .build()));

        gbc.gridx = 2;
        gbc.weightx = 0.28;
        panel.add(fromUnitCombo, gbc);

        JButton swapButton = new JButton("⇄");
        swapButton.setToolTipText("Intercambiar unidades");
        swapButton.setFont(new Font("Dialog", Font.BOLD, 15));
        swapButton.setPreferredSize(new Dimension(58, 30));
        swapButton.addActionListener(e -> {
            swapUnits();
            clearResultOnly();
        });

        gbc.gridx = 3;
        gbc.weightx = 0.03;
        panel.add(swapButton, gbc);

        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setFont(new Font("Verdana", Font.BOLD, 13));
        resultField.setBackground(Color.WHITE);
        resultField.setPreferredSize(new Dimension(160, 30));

        gbc.gridx = 4;
        gbc.weightx = 0.16;
        panel.add(resultField, gbc);

        toUnitCombo = new JComboBox<>();
        toUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        toUnitCombo.setPreferredSize(new Dimension(250, 30));
        toUnitCombo.setPrototypeDisplayValue(new UnitOption(UnitInfo.newBuilder()
                .setCode("g_cm3")
                .setLabel("Gramo por centimetro cubico")
                .build()));

        gbc.gridx = 5;
        gbc.weightx = 0.28;
        panel.add(toUnitCombo, gbc);

        JButton convertButton = new JButton("Convertir");
        convertButton.setFont(new Font("Verdana", Font.BOLD, 13));
        convertButton.setPreferredSize(new Dimension(145, 32));
        convertButton.addActionListener(e -> convert());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(convertButton, gbc);

        JLabel hintLabel = new JLabel("Selecciona una categoria, escribe el valor y elige las unidades. El calculo se realiza mediante gRPC.");
        hintLabel.setForeground(new Color(90, 90, 90));
        hintLabel.setFont(new Font("Verdana", Font.PLAIN, 11));

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 5;
        gbc.weightx = 1.0;
        panel.add(hintLabel, gbc);

        valueField.addActionListener(e -> convert());

        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(BLUE);
        label.setFont(new Font("Verdana", Font.BOLD, 11));
        return label;
    }

    private void clearResultOnly() {
        if (resultField != null) {
            resultField.setText("");
        }
    }
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 203, 255)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setPreferredSize(new Dimension(1000, 300));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel symbolsPanel = new JPanel(new BorderLayout(6, 6));
        symbolsPanel.setBackground(Color.WHITE);
        symbolsPanel.setPreferredSize(new Dimension(330, 260));
        symbolsPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel symbolsTitle = new JLabel("Simbolo de las unidades", SwingConstants.CENTER);
        symbolsTitle.setForeground(BLUE);
        symbolsTitle.setFont(new Font("Verdana", Font.PLAIN, 16));

        symbolPane = new JEditorPane();
        symbolPane.setContentType("text/html");
        symbolPane.setEditable(false);
        symbolPane.setOpaque(false);
        symbolPane.setFont(new Font("Verdana", Font.PLAIN, 12));
        symbolPane.setText("<html><body style='font-family:Verdana;font-size:11px;'>Seleccione una categoria.</body></html>");

        symbolsPanel.add(symbolsTitle, BorderLayout.NORTH);
        symbolsPanel.add(symbolPane, BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new BorderLayout(8, 8));
        historyPanel.setBackground(Color.WHITE);

        JLabel historyTitle = new JLabel("Historial de conversiones");
        historyTitle.setForeground(BLUE);
        historyTitle.setFont(new Font("Verdana", Font.BOLD, 14));
        historyPanel.add(historyTitle, BorderLayout.NORTH);

        historyArea = new JTextArea(13, 70);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        historyArea.setBackground(new Color(250, 250, 250));

        historyPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);

        JButton clearButton = new JButton("Limpiar historial");
        clearButton.addActionListener(e -> historyArea.setText(""));

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetForm());

        buttons.add(clearButton);
        buttons.add(resetButton);

        historyPanel.add(buttons, BorderLayout.SOUTH);

        panel.add(symbolsPanel, BorderLayout.WEST);
        panel.add(historyPanel, BorderLayout.CENTER);

        return panel;
    }
    private void loadCatalog() {
        try {
            CatalogResponse catalog = stub.getCatalog(CatalogRequest.newBuilder().build());

            loadedCategories.clear();
            loadedCategories.addAll(catalog.getCategoriesList());

            categoryCombo.removeAllItems();
            for (CategoryInfo category : loadedCategories) {
                categoryCombo.addItem(new CategoryOption(category));
            }

            refreshMenuLinks();

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
        updateSymbols(category);
    }

    private void updateSymbols(CategoryInfo category) {
        if (symbolPane == null || category == null) {
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Verdana;font-size:11px;margin:6px;'>");
        html.append("<table width='100%'>");

        int column = 0;
        for (UnitInfo unit : category.getUnitsList()) {
            if (column == 0) {
                html.append("<tr>");
            }

            String code = unit.getCode();
            String label = unit.getLabel();

            int parenthesisIndex = label.indexOf(" (");
            if (parenthesisIndex > 0) {
                label = label.substring(0, parenthesisIndex);
            }

            html.append("<td style='padding:4px 10px 4px 0; vertical-align:top;'>");
            html.append("<b>").append(code).append("</b>, ");
            html.append(label);
            html.append("</td>");

            column++;

            if (column == 2) {
                html.append("</tr>");
                column = 0;
            }
        }

        if (column != 0) {
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</body></html>");

        symbolPane.setText(html.toString());
        symbolPane.setCaretPosition(0);
    }

    private void selectCategory(String categoryCode) {
        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            CategoryOption option = categoryCombo.getItemAt(i);
            if (option.category().getCode().equalsIgnoreCase(categoryCode)) {
                categoryCombo.setSelectedIndex(i);
                syncCategoryLinks();
                return;
            }
        }
    }

    private void syncCategoryLinks() {
        CategoryOption selected = (CategoryOption) categoryCombo.getSelectedItem();
        String activeCode = selected == null ? "" : selected.category().getCode();

        for (Map.Entry<String, JButton> entry : categoryLinkButtons.entrySet()) {
            JButton button = entry.getValue();
            boolean active = entry.getKey().equalsIgnoreCase(activeCode);

            button.setForeground(active ? DARK_BLUE : BLUE);
            button.setBackground(active ? ACTIVE_LINK_BG : PANEL_BACKGROUND);
            button.setBorder(active
                    ? BorderFactory.createLineBorder(new Color(180, 210, 240))
                    : BorderFactory.createEmptyBorder(2, 4, 2, 4));
        }
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
            String message = "Selecciona categoria y unidades.";
            resultField.setText("");
            historyArea.append("[ERROR] " + message + "\n");
            statusLabel.setText("Datos incompletos");
            statusLabel.setForeground(Color.RED);
            showErrorPopup(message);
            return;
        }

        double value;
        try {
            value = Double.parseDouble(valueField.getText().trim());
        } catch (NumberFormatException ex) {
            String message = "Ingresa un numero valido.";
            resultField.setText("");
            historyArea.append("[ERROR] " + message + "\n");
            statusLabel.setText("Entrada no valida");
            statusLabel.setForeground(Color.RED);
            showErrorPopup(message);
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
                String message = response.getErrorMessage();
                if (message == null || message.isBlank()) {
                    message = "La conversion no es valida.";
                }

                resultField.setText("");
                historyArea.append("[ERROR] " + message + "\n");
                statusLabel.setText("Conversion no valida");
                statusLabel.setForeground(Color.RED);
                showErrorPopup(message);
            }
        } catch (StatusRuntimeException ex) {
            String message = "No se pudo conectar con el servidor gRPC.";
            resultField.setText("");
            historyArea.append("[ERROR DE RED] " + ex.getStatus() + "\n");
            statusLabel.setText("Sin conexion con el servidor");
            statusLabel.setForeground(Color.RED);
            showErrorPopup(message);
        }
    }

    private void showErrorPopup(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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
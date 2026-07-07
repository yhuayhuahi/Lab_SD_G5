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
        setSize(1040, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(PAGE_BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        page.add(createHeader());
        page.add(Box.createVerticalStrut(14));
        page.add(createMenuPanel());
        page.add(Box.createVerticalStrut(16));
        page.add(createConverterPanel());
        page.add(Box.createVerticalStrut(14));
        page.add(createHistoryPanel());
        page.add(Box.createVerticalStrut(8));

        statusLabel = new JLabel("Conectando con servidor gRPC en localhost:50051");
        statusLabel.setFont(new Font("Verdana", Font.ITALIC, 11));
        statusLabel.setForeground(BLUE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(statusLabel);

        add(page);
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BLUE);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        wrapper.setPreferredSize(new Dimension(1040, 86));

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));

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
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 205));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(241, 241, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 203, 255)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel section = new JLabel("Conversor de Unidades Online", SwingConstants.CENTER);
        section.setForeground(BLUE);
        section.setFont(new Font("Verdana", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 6;
        panel.add(section, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        categoryCombo.addActionListener(e -> {
            updateUnitCombos();
            syncCategoryLinks();
        });
        gbc.gridx = 0;
        gbc.weightx = 0.55;
        panel.add(categoryCombo, gbc);

        valueField = new JTextField("100");
        valueField.setFont(new Font("Verdana", Font.PLAIN, 13));
        gbc.gridx = 1;
        gbc.weightx = 0.25;
        panel.add(valueField, gbc);

        fromUnitCombo = new JComboBox<>();
        fromUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        panel.add(fromUnitCombo, gbc);

        JButton swapButton = new JButton("⇄");
        swapButton.setToolTipText("Intercambiar unidades");
        swapButton.setFont(new Font("Dialog", Font.BOLD, 14));
        swapButton.addActionListener(e -> swapUnits());
        gbc.gridx = 3;
        gbc.weightx = 0;
        panel.add(swapButton, gbc);

        toUnitCombo = new JComboBox<>();
        toUnitCombo.setFont(new Font("Verdana", Font.PLAIN, 12));
        gbc.gridx = 4;
        gbc.weightx = 1.0;
        panel.add(toUnitCombo, gbc);

        JButton convertButton = new JButton("Convertir");
        convertButton.setFont(new Font("Verdana", Font.BOLD, 13));
        convertButton.addActionListener(e -> convert());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(convertButton, gbc);

        resultField = new JTextField();
        resultField.setEditable(false);
        resultField.setFont(new Font("Verdana", Font.BOLD, 13));
        resultField.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel.add(resultField, gbc);

        valueField.addActionListener(e -> convert());

        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(155, 203, 255)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Historial de conversiones");
        title.setForeground(BLUE);
        title.setFont(new Font("Verdana", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        historyArea = new JTextArea(9, 70);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        historyArea.setBackground(new Color(250, 250, 250));

        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);

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
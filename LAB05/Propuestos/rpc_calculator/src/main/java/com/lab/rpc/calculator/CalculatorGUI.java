package com.lab.rpc.calculator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.rmi.Naming;
import java.text.DecimalFormat;

public class CalculatorGUI extends JFrame {

    private static final Color BODY = new Color(43, 47, 53);
    private static final Color BODY_DARK = new Color(28, 31, 36);
    private static final Color LCD = new Color(178, 194, 164);
    private static final Color LCD_DARK = new Color(56, 71, 56);
    private static final Color KEY = new Color(82, 88, 98);
    private static final Color KEY_HOVER = new Color(104, 112, 126);
    private static final Color KEY_PRESS = new Color(58, 63, 71);
    private static final Color OPERATOR = new Color(66, 73, 83);
    private static final Color RED_KEY = new Color(157, 38, 62);
    private static final Color RED_KEY_HOVER = new Color(185, 55, 82);

    private final DecimalFormat decimalFormat = new DecimalFormat("0.##########");

    private Calculator calculator;
    private JLabel display;
    private JLabel miniDisplay;

    private String currentInput = "0";
    private Double firstOperand = null;
    private String pendingOperation = null;
    private boolean resetInput = false;

    public CalculatorGUI() {
        connectToServer();
        initUI();
    }

    private void connectToServer() {
        try {
            calculator = (Calculator) Naming.lookup(CalculatorServer.SERVICE_URL);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar con el servidor RPC.\nPrimero ejecuta CalculatorServer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void initUI() {
        setTitle("Calculadora RPC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 720);
        setMinimumSize(new Dimension(410, 680));
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new RoundedPanel(26, BODY);
        root.setLayout(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createKeyboard(), BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel brand = new JLabel("CASIO");
        brand.setForeground(new Color(220, 230, 235));
        brand.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel model = new JLabel("fx-RPC · Java RMI");
        model.setForeground(new Color(220, 220, 220));
        model.setFont(new Font("Arial", Font.PLAIN, 13));
        model.setHorizontalAlignment(SwingConstants.RIGHT);

        top.add(brand, BorderLayout.WEST);
        top.add(model, BorderLayout.EAST);

        JPanel solar = new JPanel();
        solar.setPreferredSize(new Dimension(160, 42));
        solar.setBackground(new Color(77, 52, 42));
        solar.setBorder(BorderFactory.createLineBorder(new Color(20, 20, 20), 3));

        JPanel displayPanel = new RoundedPanel(10, LCD);
        displayPanel.setLayout(new BorderLayout(4, 4));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        displayPanel.setPreferredSize(new Dimension(370, 105));

        miniDisplay = new JLabel("RPC READY");
        miniDisplay.setForeground(LCD_DARK);
        miniDisplay.setFont(new Font("Monospaced", Font.ITALIC, 13));

        display = new JLabel("0");
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setForeground(new Color(33, 44, 35));
        display.setFont(resolveDisplayFont());

        displayPanel.add(miniDisplay, BorderLayout.NORTH);
        displayPanel.add(display, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);
        header.add(solar, BorderLayout.EAST);
        header.add(displayPanel, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.CENTER);
        return panel;
    }

    private Font resolveDisplayFont() {
        String[] preferredFamilies = {"Calculator", "Digital-7", "DS-Digital", "Monospaced"};

        for (String family : preferredFamilies) {
            if (isFontAvailable(family)) {
                return new Font(family, Font.ITALIC, 38);
            }
        }

        return new Font("Monospaced", Font.ITALIC, 38);
    }

    private boolean isFontAvailable(String target) {
        String[] families = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String family : families) {
            if (family.equalsIgnoreCase(target)) {
                return true;
            }
        }

        return false;
    }

    private JPanel createKeyboard() {
        JPanel keyboard = new JPanel(new GridBagLayout());
        keyboard.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        addButton(keyboard, gbc, "x²", 0, 0, KEY, () -> unarySquare());
        addButton(keyboard, gbc, "xʸ", 1, 0, KEY, () -> setOperation("pow"));
        addButton(keyboard, gbc, "DEL", 2, 0, RED_KEY, () -> deleteLast());
        addButton(keyboard, gbc, "AC", 3, 0, RED_KEY, () -> clearAll());

        addButton(keyboard, gbc, "7", 0, 1, KEY, () -> appendDigit("7"));
        addButton(keyboard, gbc, "8", 1, 1, KEY, () -> appendDigit("8"));
        addButton(keyboard, gbc, "9", 2, 1, KEY, () -> appendDigit("9"));
        addButton(keyboard, gbc, "÷", 3, 1, OPERATOR, () -> setOperation("divide"));

        addButton(keyboard, gbc, "4", 0, 2, KEY, () -> appendDigit("4"));
        addButton(keyboard, gbc, "5", 1, 2, KEY, () -> appendDigit("5"));
        addButton(keyboard, gbc, "6", 2, 2, KEY, () -> appendDigit("6"));
        addButton(keyboard, gbc, "×", 3, 2, OPERATOR, () -> setOperation("multiply"));

        addButton(keyboard, gbc, "1", 0, 3, KEY, () -> appendDigit("1"));
        addButton(keyboard, gbc, "2", 1, 3, KEY, () -> appendDigit("2"));
        addButton(keyboard, gbc, "3", 2, 3, KEY, () -> appendDigit("3"));
        addButton(keyboard, gbc, "-", 3, 3, OPERATOR, () -> setOperation("subtract"));

        addButton(keyboard, gbc, "0", 0, 4, KEY, () -> appendDigit("0"));
        addButton(keyboard, gbc, ".", 1, 4, KEY, () -> appendDecimal());
        addButton(keyboard, gbc, "=", 2, 4, OPERATOR, () -> calculateResult());
        addButton(keyboard, gbc, "+", 3, 4, OPERATOR, () -> setOperation("add"));

        return keyboard;
    }

    private void addButton(JPanel panel, GridBagConstraints gbc, String text, int x, int y, Color baseColor, Runnable action) {
        AnimatedCalcButton button = new AnimatedCalcButton(text, baseColor);
        button.addActionListener(e -> action.run());

        gbc.gridx = x;
        gbc.gridy = y;

        panel.add(button, gbc);
    }

    private void appendDigit(String digit) {
        if (resetInput || "0".equals(currentInput)) {
            currentInput = digit;
            resetInput = false;
        } else {
            currentInput += digit;
        }

        updateDisplay();
    }

    private void appendDecimal() {
        if (resetInput) {
            currentInput = "0";
            resetInput = false;
        }

        if (!currentInput.contains(".")) {
            currentInput += ".";
        }

        updateDisplay();
    }

    private void deleteLast() {
        if (resetInput) {
            currentInput = "0";
            resetInput = false;
        } else if (currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        } else {
            currentInput = "0";
        }

        updateDisplay();
    }

    private void clearAll() {
        currentInput = "0";
        firstOperand = null;
        pendingOperation = null;
        resetInput = false;
        miniDisplay.setText("RPC READY");
        updateDisplay();
    }

    private void setOperation(String operation) {
        try {
            firstOperand = parseCurrentInput();
            pendingOperation = operation;
            resetInput = true;
            miniDisplay.setText(format(firstOperand) + " " + symbolFor(operation));
        } catch (NumberFormatException ex) {
            showError("Entrada no valida.");
        }
    }

    private void unarySquare() {
        try {
            double value = parseCurrentInput();
            long start = System.nanoTime();
            double result = calculator.power(value, 2);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            currentInput = format(result);
            miniDisplay.setText(format(value) + "²  |  " + elapsedMs + " ms");
            resetInput = true;
            updateDisplay();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void calculateResult() {
        if (firstOperand == null || pendingOperation == null) {
            return;
        }

        try {
            double secondOperand = parseCurrentInput();

            long start = System.nanoTime();
            double result = executeRemoteOperation(firstOperand, secondOperand, pendingOperation);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            miniDisplay.setText(
                    format(firstOperand) + " " + symbolFor(pendingOperation) + " " + format(secondOperand)
                            + "  |  " + elapsedMs + " ms"
            );

            currentInput = format(result);
            firstOperand = null;
            pendingOperation = null;
            resetInput = true;
            updateDisplay();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private double executeRemoteOperation(double a, double b, String operation) throws Exception {
        if (calculator == null) {
            throw new IllegalStateException("Servidor RPC no disponible.");
        }

        return switch (operation) {
            case "add" -> calculator.add(a, b);
            case "subtract" -> calculator.subtract(a, b);
            case "multiply" -> calculator.multiply(a, b);
            case "divide" -> calculator.divide(a, b);
            case "pow" -> calculator.power(a, b);
            default -> throw new IllegalArgumentException("Operacion no reconocida.");
        };
    }

    private double parseCurrentInput() {
        return Double.parseDouble(currentInput);
    }

    private void updateDisplay() {
        display.setText(currentInput);
    }

    private String format(double value) {
        return decimalFormat.format(value);
    }

    private String symbolFor(String operation) {
        return switch (operation) {
            case "add" -> "+";
            case "subtract" -> "-";
            case "multiply" -> "×";
            case "divide" -> "÷";
            case "pow" -> "^";
            default -> "?";
        };
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message == null ? "Error desconocido." : message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class RoundedPanel extends JPanel {

        private final int radius;
        private final Color color;

        RoundedPanel(int radius, Color color) {
            this.radius = radius;
            this.color = color;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AnimatedCalcButton extends JButton {

        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressColor;

        AnimatedCalcButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = RED_KEY.equals(baseColor) ? RED_KEY_HOVER : KEY_HOVER;
            this.pressColor = KEY_PRESS;

            setFont(new Font("Arial", Font.BOLD, 22));
            setForeground(Color.WHITE);
            setBackground(baseColor);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(18, 18, 18), 2),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(78, 58));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(hoverColor);
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(baseColor);
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    setBackground(pressColor);
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    setBackground(hoverColor);
                    Timer timer = new Timer(90, event -> setBackground(hoverColor));
                    timer.setRepeats(false);
                    timer.start();
                }
            });
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new CalculatorGUI().setVisible(true));
    }
}
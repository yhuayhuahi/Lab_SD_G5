package com.lab.rpc.calculator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.rmi.Naming;
import java.text.DecimalFormat;

public class CalculatorGUI extends JFrame {

    private static final Color BODY = new Color(43, 47, 53);
    private static final Color BODY_DARK = new Color(28, 31, 36);
    private static final Color LCD = new Color(178, 194, 164);
    private static final Color LCD_DARK = new Color(56, 71, 56);
    private static final Color KEY = new Color(78, 86, 98);
    private static final Color KEY_HOVER = new Color(104, 114, 130);
    private static final Color KEY_PRESS = new Color(48, 54, 63);
    private static final Color OPERATOR = new Color(92, 100, 112);
    private static final Color OPERATOR_HOVER = new Color(120, 130, 145);
    private static final Color RED_KEY = new Color(166, 35, 65);
    private static final Color RED_KEY_HOVER = new Color(200, 55, 90);

    private final DecimalFormat decimalFormat = new DecimalFormat("0.##########");

    private Calculator calculator;
    private JLabel display;
    private JLabel miniDisplay;

    private String currentInput = "0";
    private Double firstOperand = null;
    private String pendingOperation = null;
    private boolean resetInput = false;

    public CalculatorGUI() {
        initUI();
        connectToServer();
    }

    private void connectToServer() {
        try {
            calculator = (Calculator) Naming.lookup(CalculatorServer.SERVICE_URL);
            miniDisplay.setText("RPC CONNECTED");
        } catch (Exception ex) {
            miniDisplay.setText("RPC OFFLINE");
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo conectar con el servidor RPC.\nPrimero ejecuta CalculatorServer.",
                    "Error de conexion",
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
        configureKeyboardShortcuts();
    }

    private void configureKeyboardShortcuts() {
        bindKey("NUM_0", "0", () -> appendDigit("0"));
        bindKey("NUM_1", "1", () -> appendDigit("1"));
        bindKey("NUM_2", "2", () -> appendDigit("2"));
        bindKey("NUM_3", "3", () -> appendDigit("3"));
        bindKey("NUM_4", "4", () -> appendDigit("4"));
        bindKey("NUM_5", "5", () -> appendDigit("5"));
        bindKey("NUM_6", "6", () -> appendDigit("6"));
        bindKey("NUM_7", "7", () -> appendDigit("7"));
        bindKey("NUM_8", "8", () -> appendDigit("8"));
        bindKey("NUM_9", "9", () -> appendDigit("9"));

        bindKey("DECIMAL_DOT", ".", this::appendDecimal);
        bindKey("ADD", "PLUS", () -> setOperation("add"));
        bindKey("ADD_NUMPAD", "ADD", () -> setOperation("add"));
        bindKey("SUBTRACT", "MINUS", () -> setOperation("subtract"));
        bindKey("SUBTRACT_NUMPAD", "SUBTRACT", () -> setOperation("subtract"));
        bindKey("MULTIPLY", "MULTIPLY", () -> setOperation("multiply"));
        bindKey("MULTIPLY_ALT", "shift 8", () -> setOperation("multiply"));
        bindKey("DIVIDE", "SLASH", () -> setOperation("divide"));
        bindKey("DIVIDE_NUMPAD", "DIVIDE", () -> setOperation("divide"));
        bindKey("POWER", "shift 6", () -> setOperation("pow"));

        bindKey("ENTER_EQUALS", "ENTER", this::calculateResult);
        bindKey("BACKSPACE_DELETE", "BACK_SPACE", this::deleteLast);
        bindKey("ESCAPE_CLEAR", "ESCAPE", this::clearAll);
    }

    private void bindKey(String name, String keyStroke, Runnable action) {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyStroke), name);

        getRootPane().getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 10));
        header.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel brand = new JLabel("CASIO");
        brand.setForeground(new Color(235, 240, 245));
        brand.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel model = new JLabel("fx-RPC · Java RMI");
        model.setForeground(Color.WHITE);
        model.setFont(new Font("Arial", Font.PLAIN, 14));
        model.setHorizontalAlignment(SwingConstants.RIGHT);

        top.add(brand, BorderLayout.WEST);
        top.add(model, BorderLayout.EAST);

        JPanel solar = new JPanel();
        solar.setPreferredSize(new Dimension(165, 44));
        solar.setBackground(new Color(75, 50, 42));
        solar.setBorder(BorderFactory.createLineBorder(new Color(18, 18, 18), 4));

        JPanel middle = new JPanel(new BorderLayout());
        middle.setOpaque(false);
        middle.add(solar, BorderLayout.EAST);

        JPanel displayPanel = new RoundedPanel(10, LCD);
        displayPanel.setLayout(new BorderLayout(4, 4));
        displayPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        displayPanel.setPreferredSize(new Dimension(370, 112));

        miniDisplay = new JLabel("RPC READY");
        miniDisplay.setForeground(LCD_DARK);
        miniDisplay.setFont(new Font("Monospaced", Font.ITALIC, 13));

        display = new JLabel("0");
        display.setHorizontalAlignment(SwingConstants.RIGHT);
        display.setForeground(new Color(28, 39, 31));
        display.setFont(resolveDisplayFont());

        displayPanel.add(miniDisplay, BorderLayout.NORTH);
        displayPanel.add(display, BorderLayout.CENTER);

        header.add(top, BorderLayout.NORTH);
        header.add(middle, BorderLayout.CENTER);
        header.add(displayPanel, BorderLayout.SOUTH);

        return header;
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

        addButton(keyboard, gbc, "x²", 0, 0, KEY, KEY_HOVER, () -> unarySquare());
        addButton(keyboard, gbc, "xʸ", 1, 0, KEY, KEY_HOVER, () -> setOperation("pow"));
        addButton(keyboard, gbc, "DEL", 2, 0, RED_KEY, RED_KEY_HOVER, () -> deleteLast());
        addButton(keyboard, gbc, "AC", 3, 0, RED_KEY, RED_KEY_HOVER, () -> clearAll());

        addButton(keyboard, gbc, "7", 0, 1, KEY, KEY_HOVER, () -> appendDigit("7"));
        addButton(keyboard, gbc, "8", 1, 1, KEY, KEY_HOVER, () -> appendDigit("8"));
        addButton(keyboard, gbc, "9", 2, 1, KEY, KEY_HOVER, () -> appendDigit("9"));
        addButton(keyboard, gbc, "÷", 3, 1, OPERATOR, OPERATOR_HOVER, () -> setOperation("divide"));

        addButton(keyboard, gbc, "4", 0, 2, KEY, KEY_HOVER, () -> appendDigit("4"));
        addButton(keyboard, gbc, "5", 1, 2, KEY, KEY_HOVER, () -> appendDigit("5"));
        addButton(keyboard, gbc, "6", 2, 2, KEY, KEY_HOVER, () -> appendDigit("6"));
        addButton(keyboard, gbc, "×", 3, 2, OPERATOR, OPERATOR_HOVER, () -> setOperation("multiply"));

        addButton(keyboard, gbc, "1", 0, 3, KEY, KEY_HOVER, () -> appendDigit("1"));
        addButton(keyboard, gbc, "2", 1, 3, KEY, KEY_HOVER, () -> appendDigit("2"));
        addButton(keyboard, gbc, "3", 2, 3, KEY, KEY_HOVER, () -> appendDigit("3"));
        addButton(keyboard, gbc, "-", 3, 3, OPERATOR, OPERATOR_HOVER, () -> setOperation("subtract"));

        addButton(keyboard, gbc, "0", 0, 4, KEY, KEY_HOVER, () -> appendDigit("0"));
        addButton(keyboard, gbc, ".", 1, 4, KEY, KEY_HOVER, () -> appendDecimal());
        addButton(keyboard, gbc, "=", 2, 4, OPERATOR, OPERATOR_HOVER, () -> calculateResult());
        addButton(keyboard, gbc, "+", 3, 4, OPERATOR, OPERATOR_HOVER, () -> setOperation("add"));

        return keyboard;
    }

    private void addButton(JPanel panel, GridBagConstraints gbc, String text, int x, int y, Color baseColor, Color hoverColor, Runnable action) {
        AnimatedCalcButton button = new AnimatedCalcButton(text, baseColor, hoverColor);
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
        miniDisplay.setText(calculator == null ? "RPC OFFLINE" : "RPC CONNECTED");
        updateDisplay();
    }

    private void setOperation(String operation) {
        try {
            ensureConnected();

            if (pendingOperation != null && !resetInput) {
                double secondOperand = parseCurrentInput();

                long start = System.nanoTime();
                double intermediateResult = executeRemoteOperation(firstOperand, secondOperand, pendingOperation);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;

                miniDisplay.setText(
                        format(firstOperand) + " " + symbolFor(pendingOperation) + " " + format(secondOperand)
                                + " = " + format(intermediateResult) + "  |  " + elapsedMs + " ms"
                );

                currentInput = format(intermediateResult);
                firstOperand = intermediateResult;
                updateDisplay();
            } else {
                firstOperand = parseCurrentInput();
            }

            pendingOperation = operation;
            resetInput = true;
            miniDisplay.setText(format(firstOperand) + " " + symbolFor(operation));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    private void unarySquare() {
        try {
            ensureConnected();
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
            ensureConnected();
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

    private void ensureConnected() {
        if (calculator == null) {
            connectToServer();
        }

        if (calculator == null) {
            throw new IllegalStateException("Servidor RPC no disponible.");
        }
    }

    private double executeRemoteOperation(double a, double b, String operation) throws Exception {
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AnimatedCalcButton extends JButton {

        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressColor = KEY_PRESS;

        private Color currentColor;

        AnimatedCalcButton(String text, Color baseColor, Color hoverColor) {
            super(text);
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.currentColor = baseColor;

            setFont(new Font("Arial", Font.BOLD, 22));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(78, 58));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    currentColor = baseColor;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    currentColor = pressColor;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    currentColor = hoverColor;
                    repaint();

                    Timer timer = new Timer(90, event -> {
                        currentColor = hoverColor;
                        repaint();
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(15, 17, 20));
            g2.fillRoundRect(3, 4, getWidth() - 6, getHeight() - 5, 8, 8);

            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 8, 8, 8);

            g2.setColor(new Color(180, 188, 198, 110));
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(1, 1, getWidth() - 8, getHeight() - 10, 8, 8);

            g2.dispose();
            super.paintComponent(graphics);
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.invokeLater(() -> new CalculatorGUI().setVisible(true));
    }
}
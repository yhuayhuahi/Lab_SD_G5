package org.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    public CalculatorImpl() throws RemoteException {
        super();
    }

    @Override
    public double calculate(String operation, double a, double b) throws RemoteException {
        double result = switch (operation) {
            case "add" -> a + b;
            case "subtract" -> a - b;
            case "multiply" -> a * b;
            case "divide" -> {
                if (b == 0) {
                    throw new ArithmeticException("No se puede dividir entre cero.");
                }
                yield a / b;
            }
            case "power" -> Math.pow(a, b);
            default -> throw new IllegalArgumentException("Operacion no reconocida: " + operation);
        };

        if (!Double.isFinite(result)) {
            throw new ArithmeticException("Resultado no finito.");
        }

        return result;
    }
}
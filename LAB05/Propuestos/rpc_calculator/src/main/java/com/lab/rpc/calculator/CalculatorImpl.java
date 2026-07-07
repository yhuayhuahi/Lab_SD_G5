package com.lab.rpc.calculator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    public CalculatorImpl() throws RemoteException {
        super();
    }

    @Override
    public double add(double a, double b) throws RemoteException {
        return validateResult(a + b, "suma");
    }

    @Override
    public double subtract(double a, double b) throws RemoteException {
        return validateResult(a - b, "resta");
    }

    @Override
    public double multiply(double a, double b) throws RemoteException {
        return validateResult(a * b, "multiplicacion");
    }

    @Override
    public double divide(double a, double b) throws RemoteException {
        if (b == 0) {
            throw new ArithmeticException("No se puede dividir entre cero.");
        }

        return validateResult(a / b, "division");
    }

    @Override
    public double power(double base, double exponent) throws RemoteException {
        return validateResult(Math.pow(base, exponent), "potencia");
    }

    private double validateResult(double result, String operation) {
        if (!Double.isFinite(result)) {
            throw new ArithmeticException("El resultado de la " + operation + " no es finito.");
        }

        return result;
    }
}
package org.example;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

  // El constructor debe ser explícito para declarar la excepción RemoteException
  public CalculatorImpl() throws RemoteException {
    super();
  }

  public double add(double a, double b) throws RemoteException {
    return a + b;
  }

  public double sub(double a, double b) throws RemoteException {
    return a - b;
  }

  public double mul(double a, double b) throws RemoteException {
    return a * b;
  }

  public double div(double a, double b) throws RemoteException { // lanza ArithmeticException si b es 0, que el cliente debe manejar
    return a / b;
  }
}

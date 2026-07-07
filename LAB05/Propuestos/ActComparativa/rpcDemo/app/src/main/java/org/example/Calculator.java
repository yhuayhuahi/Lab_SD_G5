package org.example;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {

    double calculate(String operation, double a, double b) throws RemoteException;
}
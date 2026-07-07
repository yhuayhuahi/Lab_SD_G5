package com.lab.rpc.calculator;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Calculator extends Remote {

    double multiply(double a, double b) throws RemoteException;

    double divide(double a, double b) throws RemoteException;

    double power(double base, double exponent) throws RemoteException;
}
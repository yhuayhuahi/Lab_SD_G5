package com.lab.rpc.calculator;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

public class CalculatorServer {

    public static final int DEFAULT_PORT = 1099;
    public static final String SERVICE_NAME = "CalculatorService";
    public static final String SERVICE_URL = "rmi://localhost/" + SERVICE_NAME;

    private static final Logger logger = Logger.getLogger(CalculatorServer.class.getName());

    public static void main(String[] args) {
        try {
            startRegistry();
            Calculator calculator = new CalculatorImpl();

            Naming.rebind(SERVICE_URL, calculator);

            logger.info("Servidor RPC tradicional iniciado en puerto " + DEFAULT_PORT);
            logger.info("Servicio registrado como " + SERVICE_URL);
            logger.info("Operaciones disponibles: multiplicacion, division y potencia");
        } catch (Exception ex) {
            logger.severe("Error en el servidor RPC: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void startRegistry() throws RemoteException {
        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);
            logger.info("Registro RMI creado en puerto " + DEFAULT_PORT);
        } catch (RemoteException ex) {
            logger.info("El registro RMI ya estaba iniciado o no pudo recrearse.");
        }
    }
}
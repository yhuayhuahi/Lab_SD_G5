package com.lab.rpc.calculator;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class CalculatorServer {

    public static final int DEFAULT_PORT = 1099;
    public static final String SERVICE_NAME = "CalculatorService";
    public static final String SERVICE_URL = "rmi://localhost/" + SERVICE_NAME;

    private static final Logger logger = Logger.getLogger(CalculatorServer.class.getName());

    public static void main(String[] args) {
        CountDownLatch keepAlive = new CountDownLatch(1);

        try {
            startRegistry();

            Calculator calculator = new CalculatorImpl();
            Naming.rebind(SERVICE_URL, calculator);

            logger.info("Servidor RPC tradicional iniciado en puerto " + DEFAULT_PORT);
            logger.info("Servicio registrado como " + SERVICE_URL);
            logger.info("Operaciones disponibles: suma, resta, multiplicacion, division y potencia");
            logger.info("Servidor esperando solicitudes. Presiona Ctrl + C para detener.");

            Runtime.getRuntime().addShutdownHook(new Thread(() ->
                    logger.info("Apagando servidor RPC tradicional")
            ));

            keepAlive.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warning("Servidor interrumpido.");
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
            logger.info("El registro RMI ya estaba iniciado. Se reutilizara el puerto " + DEFAULT_PORT);
        }
    }
}
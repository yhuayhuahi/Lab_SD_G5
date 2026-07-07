package org.example;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class RpcBenchmarkServer {

    public static final int PORT = 1099;
    public static final String SERVICE_NAME = "BenchmarkCalculator";
    public static final String SERVICE_URL = "rmi://localhost/" + SERVICE_NAME;

    private static final Logger logger = Logger.getLogger(RpcBenchmarkServer.class.getName());

    public static void main(String[] args) {
        CountDownLatch keepAlive = new CountDownLatch(1);

        try {
            try {
                LocateRegistry.createRegistry(PORT);
                logger.info("Registro RMI creado en puerto " + PORT);
            } catch (Exception ex) {
                logger.info("Registro RMI ya disponible en puerto " + PORT);
            }

            Calculator calculator = new CalculatorImpl();
            Naming.rebind(SERVICE_URL, calculator);

            logger.info("Servidor RPC comparativo iniciado.");
            logger.info("Servicio registrado como " + SERVICE_URL);
            logger.info("Esperando solicitudes. Presiona Ctrl + C para detener.");

            keepAlive.await();
        } catch (Exception ex) {
            logger.severe("Error en servidor RPC: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
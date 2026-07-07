package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class GrpcBenchmarkServer {

    public static final int PORT = 50052;

    private static final Logger logger = Logger.getLogger(GrpcBenchmarkServer.class.getName());

    public static void main(String[] args) {
        try {
            Server server = ServerBuilder
                    .forPort(PORT)
                    .addService(new CalculatorServiceImpl())
                    .build()
                    .start();

            logger.info("Servidor gRPC comparativo iniciado en puerto " + PORT);
            logger.info("Operaciones disponibles: suma, resta, multiplicacion, division y potencia");
            logger.info("Esperando solicitudes. Presiona Ctrl + C para detener.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Apagando servidor gRPC comparativo.");
                server.shutdown();
            }));

            new CountDownLatch(1).await();
        } catch (Exception ex) {
            logger.severe("Error en servidor gRPC: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    static class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

        @Override
        public void calculate(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
            OperationResponse response;

            try {
                double result = calculate(request.getOperation(), request.getA(), request.getB());

                response = OperationResponse.newBuilder()
                        .setSuccess(true)
                        .setResult(result)
                        .build();
            } catch (Exception ex) {
                response = OperationResponse.newBuilder()
                        .setSuccess(false)
                        .setErrorMessage(ex.getMessage())
                        .build();
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        private double calculate(String operation, double a, double b) {
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
}
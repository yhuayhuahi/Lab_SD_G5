package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class GrpcBenchmarkClient {

    private static final int WARMUP_ROUNDS = 20;
    private static final int MEASURE_ROUNDS = 200;

    private static final String[] OPERATIONS = {
            "add",
            "subtract",
            "multiply",
            "divide",
            "power"
    };

    private static final double[][] VALUES = {
            {120.0, 30.0},
            {450.0, 75.0},
            {12.0, 8.0},
            {144.0, 12.0},
            {2.0, 8.0}
    };

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", GrpcBenchmarkServer.PORT)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub =
                CalculatorServiceGrpc.newBlockingStub(channel);

        try {
            runBenchmark(stub, WARMUP_ROUNDS, false);
            System.gc();
            Thread.sleep(250);

            long memoryBefore = usedMemory();
            long start = System.nanoTime();

            int calls = runBenchmark(stub, MEASURE_ROUNDS, true);

            long elapsedNs = System.nanoTime() - start;
            long memoryAfter = usedMemory();

            printResult("gRPC - Protocol Buffers", calls, elapsedNs, memoryBefore, memoryAfter);
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private static int runBenchmark(CalculatorServiceGrpc.CalculatorServiceBlockingStub stub, int rounds, boolean validate) {
        int calls = 0;

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < OPERATIONS.length; i++) {
                OperationRequest request = OperationRequest.newBuilder()
                        .setOperation(OPERATIONS[i])
                        .setA(VALUES[i][0])
                        .setB(VALUES[i][1])
                        .build();

                OperationResponse response = stub.calculate(request);

                if (!response.getSuccess()) {
                    throw new IllegalStateException(response.getErrorMessage());
                }

                if (validate && !Double.isFinite(response.getResult())) {
                    throw new IllegalStateException("Resultado invalido en " + OPERATIONS[i]);
                }

                calls++;
            }
        }

        return calls;
    }

    private static void printResult(String protocol, int calls, long elapsedNs, long memoryBefore, long memoryAfter) {
        double totalMs = elapsedNs / 1_000_000.0;
        double averageMs = totalMs / calls;
        double memoryMb = Math.max(0, memoryAfter - memoryBefore) / 1024.0 / 1024.0;

        System.out.println();
        System.out.println("=== RESULTADO COMPARATIVO ===");
        System.out.println("Protocolo: " + protocol);
        System.out.println("Operaciones: suma, resta, multiplicacion, division, potencia");
        System.out.println("Warmup: " + WARMUP_ROUNDS + " rondas");
        System.out.println("Rondas medidas: " + MEASURE_ROUNDS);
        System.out.println("Llamadas totales: " + calls);
        System.out.printf("Tiempo total: %.3f ms%n", totalMs);
        System.out.printf("Promedio por llamada: %.6f ms%n", averageMs);
        System.out.printf("Memoria aproximada usada: %.3f MB%n", memoryMb);
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
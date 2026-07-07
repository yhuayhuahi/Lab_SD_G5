package org.example;

import java.rmi.Naming;
import java.util.Locale;

public class RpcBenchmarkClient {

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

        Calculator calculator = (Calculator) Naming.lookup(RpcBenchmarkServer.SERVICE_URL);

        runBenchmark(calculator, WARMUP_ROUNDS, false);
        System.gc();
        Thread.sleep(250);

        long memoryBefore = usedMemory();
        long start = System.nanoTime();

        int calls = runBenchmark(calculator, MEASURE_ROUNDS, true);

        long elapsedNs = System.nanoTime() - start;
        long memoryAfter = usedMemory();

        printResult("RPC tradicional - Java RMI", calls, elapsedNs, memoryBefore, memoryAfter);
    }

    private static int runBenchmark(Calculator calculator, int rounds, boolean validate) throws Exception {
        int calls = 0;

        for (int round = 0; round < rounds; round++) {
            for (int i = 0; i < OPERATIONS.length; i++) {
                double result = calculator.calculate(OPERATIONS[i], VALUES[i][0], VALUES[i][1]);

                if (validate && !Double.isFinite(result)) {
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
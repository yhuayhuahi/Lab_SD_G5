package com.lab.rpc.calculator;

import java.rmi.Naming;
import java.util.Scanner;

public class CalculatorClient {

    public static void main(String[] args) {
        try {
            Calculator calculator = (Calculator) Naming.lookup(CalculatorServer.SERVICE_URL);

            try (Scanner scanner = new Scanner(System.in)) {
                System.out.println("=== CLIENTE RPC TRADICIONAL ===");

                double a = readDouble(scanner, "Ingrese primer numero: ");
                double b = readDouble(scanner, "Ingrese segundo numero: ");

                long start = System.nanoTime();

                double addition = calculator.add(a, b);
                double subtraction = calculator.subtract(a, b);
                double multiplication = calculator.multiply(a, b);
                double division = calculator.divide(a, b);
                double power = calculator.power(a, b);

                long elapsedMs = (System.nanoTime() - start) / 1_000_000;

                System.out.println();
                System.out.println("=== RESULTADOS RPC ===");
                System.out.println("Suma: " + addition);
                System.out.println("Resta: " + subtraction);
                System.out.println("Multiplicacion: " + multiplication);
                System.out.println("Division: " + division);
                System.out.println("Potencia: " + power);
                System.out.println("Tiempo total: " + elapsedMs + " ms");
            }
        } catch (Exception ex) {
            System.out.println("Error en el cliente RPC:");
            System.out.println(ex.getMessage());
        }
    }

    private static double readDouble(Scanner scanner, String message) {
        while (true) {
            System.out.print(message);

            if (scanner.hasNextDouble()) {
                return scanner.nextDouble();
            }

            System.out.println("Entrada no valida. Ingresa un numero.");
            scanner.next();
        }
    }
}
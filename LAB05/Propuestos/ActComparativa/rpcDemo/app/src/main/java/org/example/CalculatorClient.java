package org.example;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;

public class CalculatorClient {

  private static final long MB = 1024L * 1024L;

  private static long usedMemoryBytes() {
    Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }

  private static String formatMb(long bytes) {
    return String.format("%.2f MB", bytes / (double) MB);
  }

  public static void main(String[] args) { // verifica que se pasen dos argumentos 
    
    double num1 = 10;
    double num2 = 5;

    try {
      Calculator c = (Calculator) Naming.lookup("rmi://localhost:1099/CalculatorService"); // busca el servicio remoto

      long initialTime = System.currentTimeMillis();
      long initialMemory = usedMemoryBytes();
      
      System.out.println("The substraction of " + num1 + " and " + num2 + " is: " + c.sub(num1, num2));
      System.out.println("The addition of " + num1 + " and " + num2 + " is: " + c.add(num1, num2));
      System.out.println("The multiplication of " + num1 + " and " + num2 + " is: " + c.mul(num1, num2));
      System.out.println("The division of " + num1 + " and " + num2 + " is: " + c.div(num1, num2));

      long finalTime = System.currentTimeMillis();
      long finalMemory = usedMemoryBytes();
      long memoryDelta = finalMemory - initialMemory;

      System.out.println("Tiempo total de ejecucion: " + (finalTime - initialTime) + " ms");
      System.out.println("Memoria inicial usada: " + formatMb(initialMemory));
      System.out.println("Memoria final usada: " + formatMb(finalMemory));
      System.out.println("Variacion neta de memoria: " + formatMb(memoryDelta));
        
    } catch (MalformedURLException murle) { // maneja excepciones específicas de RMI
      System.out.println("\nMalformedURLException: " + murle);
    } catch (RemoteException re) {
      System.out.println("\nRemoteException: " + re);
    } catch (NotBoundException nbe) {
      System.out.println("\nNotBoundException: " + nbe);
    } catch (java.lang.ArithmeticException ae) {
      System.out.println("\njava.lang.ArithmeticException: " + ae);
    }
  }
}
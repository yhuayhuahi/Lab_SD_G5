import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Servidor {
    private List<Cliente> clientes;
    private double tiempoServidor;

    public Servidor(List<Cliente> clientes, double tiempoServidor) {
        this.clientes = clientes;
        this.tiempoServidor = tiempoServidor;
    }

public void sincronizar() {
    double suma = tiempoServidor;

    System.out.println("\n[MASTER] Enviando ping a clientes...");

    ExecutorService pool = Executors.newFixedThreadPool(4); // pool de hilos para manejar clientes concurrentemente, crea 4 hilos para 4 clientes

    List<Future<Double>> resultados = new ArrayList<>();

    for (Cliente c : clientes) {
        Future<Double> future = pool.submit(() -> { // future será el resultado del cliente, se ejecuta en un hilo separado, submit recibe una lambda que representa la tarea a ejecutar
            long t0 = System.currentTimeMillis();

            double tiempoCliente = c.responderTiempo(); // se obtiene el tiempo del cliente, que incluye el delay simulado (RTT)

            long t1 = System.currentTimeMillis(); // despues de recibir la respuesta

            long RTT = t1 - t0; // entre enviar el ping y recibir respuesta
            double tiempoAjustado = tiempoCliente + (RTT / 2.0); // ajusta el tiempo del cliente sumando la mitad del rtt 

            System.out.println(c.getNombre() +
                    " | RTT: " + RTT +
                    " | tiempo ajustado: " + tiempoAjustado);

            return tiempoAjustado;
        });

        resultados.add(future);
    }

    // Esperar resultados
    for (Future<Double> f : resultados) {
        try {
            suma += f.get(); // suma el tiempo ajustado del cliente al total, get() bloquea hasta que el resultado esté disponible
        } catch (Exception e) {
            System.out.println("Error en hilo: " + e.getMessage());
        }
    }

    pool.shutdown(); // se cierra el pool de hilos

    double promedio = suma / (clientes.size() + 1); 

    System.out.println("\n[MASTER] Tiempo promedio: " + promedio);

    double ajusteServidor = promedio - tiempoServidor; 
    tiempoServidor += ajusteServidor; // se ajusta el tiempo del servidor con el ajuste calculado

    System.out.println("[MASTER] Ajuste servidor: " + ajusteServidor);

    for (Cliente c : clientes) {
        double ajuste = promedio - c.getTiempo();
        System.out.println("Ajuste para " + c.getNombre() + ": " + ajuste);
        c.ajustarTiempo(ajuste);
    }
}

    public double getTiempoServidor() {
        return tiempoServidor;
    }
}
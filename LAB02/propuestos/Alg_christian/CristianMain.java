import java.util.*;
import java.util.concurrent.*;

public class CristianMain {

    public static void main(String[] args) throws Exception {

        // Servidor con tiempo "real"
        CristianServer server = new CristianServer(100.0);

        // Crear clientes con tiempos desincronizados
        List<CristianClient> clients = new ArrayList<>();
        clients.add(new CristianClient(0, 93.0));
        clients.add(new CristianClient(1, 105.0));
        clients.add(new CristianClient(2, 97.5));
        clients.add(new CristianClient(3, 110.0));

        System.out.println("Tiempos iniciales:");
        for (CristianClient c : clients) {
            System.out.println(c);
        }
        System.out.println("Hora del servidor: " + 
            String.format("%.5f", server.getServerTime()) + " s");

        // Pool de hilos
        ExecutorService exec = Executors.newFixedThreadPool(clients.size());
        List<Future<Double>> futures = new ArrayList<>();

        // Simulación del algoritmo de Cristian
        for (int i = 0; i < clients.size(); i++) {

            futures.add(exec.submit(() -> {
                long t0 = System.nanoTime();

                // Latencia simulada
                int latency = 20 + new Random().nextInt(80);
                Thread.sleep(latency);

                double serverTime = server.getServerTime();

                long t1 = System.nanoTime();

                double rtt = (t1 - t0) / 1e9; // en segundos

                // Cristian: tiempo estimado = Ts + RTT/2
                return serverTime + rtt / 2.0;
            }));
        }

        exec.shutdown();

        // Ajuste de relojes
        System.out.println("\nAjustes realizados:");
        for (int i = 0; i < clients.size(); i++) {

            double estimatedTime = futures.get(i).get();
            CristianClient client = clients.get(i);

            double oldTime = client.getLocalTime();
            client.setLocalTime(estimatedTime);

            double delta = estimatedTime - oldTime;

            System.out.println("Cliente " + client.getId() +
                " ajustó " + String.format("%.5f", delta) +
                " s → Nuevo: " + String.format("%.5f", client.getLocalTime()) + " s");
        }

        System.out.println("\nTiempos finales:");
        for (CristianClient c : clients) {
            System.out.println(c);
        }
    }
}
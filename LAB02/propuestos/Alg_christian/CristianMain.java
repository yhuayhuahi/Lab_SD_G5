import java.util.*; // para List, ArrayList, Random
import java.util.concurrent.*; // para ExecutorService, sirve para hilos y tareas concurrentes

public class CristianMain {

    public static void main(String[] args) throws Exception {

        CristianServer server = new CristianServer(100.0); // tiempo inicial de 100 segundos

        // Crear clientes con tiempos desincronizados
        List<CristianClient> clients = new ArrayList<>(); 
        clients.add(new CristianClient(0, 93.0));
        clients.add(new CristianClient(1, 105.0)); // tiempo en segundos
        clients.add(new CristianClient(2, 97.5));
        clients.add(new CristianClient(3, 110.0));

        System.out.println("Tiempos iniciales:");
        for (CristianClient c : clients) {
            System.out.println(c);
        }
        System.out.println("Hora del servidor: " + 
            String.format("%.5f", server.getServerTime()) + " s");


        ExecutorService exec = Executors.newFixedThreadPool(clients.size()); //
        List<Future<Double>> futures = new ArrayList<>(); // guarda el tiempo de respuesta entre el cliente y el servidor


        // Simulación del algoritmo de Cristian
        for (int i = 0; i < clients.size(); i++) {

            futures.add(exec.submit(() -> { // ejecuta la tarea en un hilo del pool 
                long t0 = System.nanoTime(); // tiempo d inicio

                int latency = 20 + new Random().nextInt(80); // latencia aleatoria entre 20 y 100 ms para simular la comunicación con el servidor,
                Thread.sleep(latency); // hace dormir el hilo actual por el tiempo de latencia simuladom, tiempo que tarda la solicitud en llegar al servidor

                double serverTime = server.getServerTime(); 

                long t1 = System.nanoTime(); 

                double rtt = (t1 - t0) / 1e9; // en segundos, RTT es el tiempo total que tarda la solicitud en ir al servidor y volver, se calcula restando el tiempo de envío del tiempo de recepción

                // tiempo estimado de ajuste
                return serverTime + rtt / 2.0; // suma la mitad del RTT al tiempo del servidor para estimar el tiempo actual, se devuelve el tiempo estimado que el cliente calculará después de recibir la respuesta del servidor
            }));
        }

        exec.shutdown();

        // Ajuste de relojes
        System.out.println("\nAjustes realizados:");
        for (int i = 0; i < clients.size(); i++) {

            double estimatedTime = futures.get(i).get(); // obtiene el resultado de la tarea que se ejecutó en el executorservice
            CristianClient client = clients.get(i); //

            double oldTime = client.getLocalTime(); // tiempo anterior
            client.setLocalTime(estimatedTime); // tiempo estimado calculado, se ajusta el reloj del cliente al tiempo estimado

            double delta = estimatedTime - oldTime; // diferencia entre el nuevo tiempo estimado y el tiempo local anterior del cliente, se calcula la diferencia entre el nuevo tiempo estimado y el tiempo local anterior del cliente para mostrar cuánto se ajustó el reloj

            System.out.println("Cliente " + client.getId() +
                " ajustó " + String.format("%.5f", delta) +
                " s --> Nuevo: " + String.format("%.5f", client.getLocalTime()) + " s");
        }

        System.out.println("\nTiempos finales:");
        for (CristianClient c : clients) {
            System.out.println(c);
        }
    }
}
import java.util.ArrayList;
import java.util.List;

public class LamportClock {
    private int clock;

    public LamportClock() { // el reloj de Lamport se inicializa en 0
        this.clock = 0;
    }

    public synchronized int tick() { // cada vez que ocurre un evento local, 
        this.clock++;
        return this.clock;
    }

    public synchronized void update(int receivedTime) { // cuando un proceso recibe un mensaje con un timestamp de Lamport, actualiza su reloj local tomando el máximo entre su reloj actual y el timestamp recibido, y luego incrementándolo en 1 para reflejar el evento de recepción del mensaje, se sincroniza para evitar condiciones de carrera al actualizar el reloj
        this.clock = Math.max(this.clock, receivedTime) + 1;
    }

    public int getTime() {
        return this.clock;
    }

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>(); 
        LamportClock clock = new LamportClock(); 

        for (int i = 0; i < 5; i++) { //
            Thread thread = new Thread(new Runnable() { // usa runnable para definir la tarea que cada hilo ejecutará, en este caso simulará eventos locales y la recepción de mensajes con timestamps de Lamport
                @Override
                public void run() {
                    int time = clock.tick();
                    System.out.println("Thread " + Thread.currentThread().getId() + " created event with Lamport time " + time); // su nombre esta compuesto por el id del hilo y el tiempo de Lamport después de generar un evento local, se muestra el tiempo de Lamport cada vez que un hilo genera un evento local
                    try {
                        Thread.sleep((long) (Math.random() * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int receivedTime = clock.tick();
                    System.out.println("Thread " + Thread.currentThread().getId() + " received event with Lamport time " + receivedTime);
                    clock.update(receivedTime);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Final Lamport time: " + clock.getTime());
    }
}

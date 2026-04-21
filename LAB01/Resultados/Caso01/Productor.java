public class Productor extends Thread {
    private CubbyHole cubbyhole;
    private int numero;

    public Productor(CubbyHole c, int numero) {
        cubbyhole = c;
        this.numero = numero;
    }

    public void run() {
        for (int i = 0; i < 50; i++) {
            cubbyhole.put(i);
            System.out.println("Productor #" + this.numero + " pone:" + i);

            try {
                sleep(200); //  lento
            } catch (InterruptedException e) { }
        }
    }
}
public class Cliente {
    private String nombre;
    private double tiempo;

    public Cliente(String nombre, double tiempo) {
        this.nombre = nombre;
        this.tiempo = tiempo;
    }

    public double getTiempo() {
        return tiempo;
    }

    public String getNombre() {
        return nombre;
    }

    // RTT
    public double responderTiempo() throws Exception {   
        try {
            int delay = (int)(Math.random() * 100); // hasta 100ms
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new Exception("Error en delay del cliente " + nombre);
        }

        return tiempo; // se devuelve el tiempo del cliente, que es lo que el servidor usará para calcular el ajuste
    }

    public void ajustarTiempo(double ajuste) { // se ajusta el tiempo del cliente con el ajuste calculado
        this.tiempo += ajuste;
    }

    public void mostrarTiempo() {
        System.out.println(nombre + " -> " + tiempo);
    }
}
public class CristianClient {
    private int id; 
    private double localTime;

    public CristianClient(int id, double startTime) {
        this.id = id;
        this.localTime = startTime; // tiempo inicial del cliente, se otorga cuando se crea el cliente en el servidor
    }

    public double getLocalTime() {
        return localTime;
    }

    public void setLocalTime(double newTime) {
        this.localTime = newTime; // se actualiza el tiempo local del cliente después de recibir la respuesta del servidor
    }

    public int getId() { 
        return id;
    }

    @Override
    public String toString() { // para mostrar el estado del cliente
        return "Cliente " + id + ": " + String.format("%.5f", localTime) + " s";
    }
}
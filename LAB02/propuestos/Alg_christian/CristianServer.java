public class CristianServer {
    private double serverTime; // tiempo del servidor en segundos, se inicializa al crear el servidor

    public CristianServer(double startTime) {
        this.serverTime = startTime;
    }

    public double getServerTime() {
        return serverTime;
    }
}
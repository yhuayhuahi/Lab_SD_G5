public class CristianClient {
    private int id;
    private double localTime;

    public CristianClient(int id, double startTime) {
        this.id = id;
        this.localTime = startTime;
    }

    public double getLocalTime() {
        return localTime;
    }

    public void setLocalTime(double newTime) {
        this.localTime = newTime;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Cliente " + id + ": " + String.format("%.5f", localTime) + " s";
    }
}
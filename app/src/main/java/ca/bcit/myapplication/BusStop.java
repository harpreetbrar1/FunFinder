package ca.bcit.myapplication;

/**
 * Class to represent a bus stop
 */
public class BusStop {

    private String stop_code;

    private String X;

    private String Y;

    public BusStop(String name, String X, String Y) {
        this.stop_code = name;
        this.X = X;
        this.Y = Y;
    }

    public String getStop_code() {
        return this.stop_code;
    }

    public String getX() {
        return this.X;
    }

    public String getY() {
        return this.Y;
    }
}

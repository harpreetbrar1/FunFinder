package ca.bcit.myapplication;

/**
 * Class to represent a piece of art
 */
public class Art {
    private String name;
    private String X;
    private String Y;


    public Art(String name, String X, String Y) {
        this.X = X;
        this.Y = Y;
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getX() {
        return X;
    }

    public void setX(String x) {
        X = x;
    }

    public String getY() {
        return Y;
    }

    public void setY(String y) {
        Y = y;
    }


}

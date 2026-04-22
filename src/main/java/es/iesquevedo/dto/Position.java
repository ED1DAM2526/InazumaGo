package es.iesquevedo.dto;

public class Position {
    private double x;
    private double y;

    // Constructores
    public Position() {}

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Getters y Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }
}

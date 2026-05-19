package es.iesquevedo.dto;

/**
 * Clase que representa una posición bidimensional en el tablero de Inazuma Go.
 *
 * <p>Esta clase encapsula las coordenadas x e y de un punto en el juego,
 * permitiendo identificar la ubicación exacta de fichas o movimientos.</p>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>
 *   Position posicion = new Position(3.5, 4.2);
 *   System.out.println(posicion.getX()); // Salida: 3.5
 *   System.out.println(posicion.getY()); // Salida: 4.2
 * </pre>
 *
 * @author [BreinnerImbachi]
 * @version 1.0
 * @since 1.0
 */
public class Position {
    /** Coordenada X de la posición. */
    private double x;

    /** Coordenada Y de la posición. */
    private double y;

    /**
     * Constructor por defecto que crea una posición en (0, 0).
     */
    public Position() {}

    /**
     * Constructor que crea una posición con coordenadas específicas.
     *
     * @param x la coordenada X de la posición
     * @param y la coordenada Y de la posición
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Obtiene la coordenada X de esta posición.
     *
     * @return la coordenada X
     */
    public double getX() { return x; }

    /**
     * Establece la coordenada X de esta posición.
     *
     * @param x la nueva coordenada X
     */
    public void setX(double x) { this.x = x; }

    /**
     * Obtiene la coordenada Y de esta posición.
     *
     * @return la coordenada Y
     */
    public double getY() { return y; }

    /**
     * Establece la coordenada Y de esta posición.
     *
     * @param y la nueva coordenada Y
     */
    public void setY(double y) { this.y = y; }

    /**
     * Retorna una representación en String de esta posición.
     *
     * @return una String con formato "Position{x=..., y=...}"
     */
    @Override
    public String toString() {
        return "Position{" + "x=" + x + ", y=" + y + '}';
    }
}


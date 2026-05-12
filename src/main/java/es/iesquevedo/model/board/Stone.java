package es.iesquevedo.model.board;

/**
 * Representa el estado de una intersección en el tablero
 */
public enum Stone {
    EMPTY(0, "·"),
    BLACK(1, "●"),
    WHITE(2, "○");

    private final int value;
    private final String symbol;

    Stone(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    public int getValue() {
        return value;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * Devuelve el color opuesto
     */
    public Stone opponent() {
        switch (this) {
            case BLACK:
                return WHITE;
            case WHITE:
                return BLACK;
            default:
                return EMPTY;
        }
    }

    /**
     * Indica si es una piedra (no vacío)
     */
    public boolean isStone() {
        return this != EMPTY;
    }
}


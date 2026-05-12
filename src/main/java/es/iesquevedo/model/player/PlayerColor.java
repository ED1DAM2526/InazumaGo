package es.iesquevedo.model.player;

import es.iesquevedo.model.board.Stone;

/**
 * Representa el color de un jugador
 */
public enum PlayerColor {
    BLACK("Negro"),
    WHITE("Blanco");

    private final String displayName;

    PlayerColor(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retorna el color opuesto
     */
    public PlayerColor opponent() {
        return this == BLACK ? WHITE : BLACK;
    }

    /**
     * Convierte a Stone
     */
    public Stone toStone() {
        return this == BLACK ? Stone.BLACK : Stone.WHITE;
    }

    public static PlayerColor fromStone(Stone stone) {
        if (stone == Stone.BLACK) return BLACK;
        if (stone == Stone.WHITE) return WHITE;
        throw new IllegalArgumentException("Cannot convert EMPTY to PlayerColor");
    }
}


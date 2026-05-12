package es.iesquevedo.model.board;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Value Object representando una coordenada (fila, columna) en el tablero 9x9
 */
public class Position {
    private static final int BOARD_SIZE = 9;
    private static final Position[][] CACHE = new Position[BOARD_SIZE][BOARD_SIZE];

    static {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                CACHE[r][c] = new Position(r, c, true);
            }
        }
    }

    private final int row;
    private final int col;

    private Position(int row, int col, boolean cached) {
        this.row = row;
        this.col = col;
    }

    /**
     * Factory method con validación y caché
     */
    public static Position of(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException(
                String.format("Position out of bounds: (%d, %d)", row, col)
            );
        }
        return CACHE[row][col];
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Devuelve los 4 vecinos ortogonales (N, S, E, O) que estén en el tablero
     */
    public List<Position> getOrthogonalNeighbors() {
        List<Position> neighbors = new ArrayList<>(4);
        
        // Norte
        if (row > 0) neighbors.add(Position.of(row - 1, col));
        // Sur
        if (row < BOARD_SIZE - 1) neighbors.add(Position.of(row + 1, col));
        // Oeste
        if (col > 0) neighbors.add(Position.of(row, col - 1));
        // Este
        if (col < BOARD_SIZE - 1) neighbors.add(Position.of(row, col + 1));
        
        return neighbors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", row, col);
    }

    /**
     * Notación Go: a1-i9 (columna letra, fila número)
     */
    public String toGoNotation() {
        char colChar = (char) ('a' + col);
        return String.format("%c%d", colChar, BOARD_SIZE - row);
    }
}


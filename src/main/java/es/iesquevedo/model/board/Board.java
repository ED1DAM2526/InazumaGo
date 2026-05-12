package es.iesquevedo.model.board;

import java.util.HashMap;
import java.util.Map;

/**
 * Tablero de 9x9 intersecciones de Inazuma Go
 */
public class Board {
    private static final int SIZE = 9;
    private final Stone[][] grid;

    public Board() {
        this.grid = new Stone[SIZE][SIZE];
        // Inicializar todas las posiciones como vacías
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = Stone.EMPTY;
            }
        }
    }

    private Board(Stone[][] grid) {
        this.grid = new Stone[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                this.grid[r][c] = grid[r][c];
            }
        }
    }

    /**
     * Obtiene la piedra en una posición
     */
    public Stone getStone(Position pos) {
        return grid[pos.getRow()][pos.getCol()];
    }

    /**
     * Coloca una piedra en una posición (debe estar vacía)
     */
    public void placeStone(Position pos, Stone stone) {
        if (stone == Stone.EMPTY) {
            throw new IllegalArgumentException("Cannot place EMPTY stone");
        }
        if (getStone(pos) != Stone.EMPTY) {
            throw new IllegalArgumentException("Position already occupied: " + pos);
        }
        grid[pos.getRow()][pos.getCol()] = stone;
    }

    /**
     * Retira una piedra de una posición
     */
    public void removeStone(Position pos) {
        grid[pos.getRow()][pos.getCol()] = Stone.EMPTY;
    }

    /**
     * Verifica si una posición está vacía
     */
    public boolean isEmpty(Position pos) {
        return getStone(pos) == Stone.EMPTY;
    }

    /**
     * Cuenta piedras de un color
     */
    public int countStones(Stone color) {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == color) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Crea una copia profunda del tablero
     */
    public Board copy() {
        return new Board(this.grid);
    }

    /**
     * Verifica si el tablero está vacío
     */
    public boolean isEmpty() {
        return countStones(Stone.BLACK) == 0 && countStones(Stone.WHITE) == 0;
    }

    /**
     * Obtiene representación textual del tablero
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h i\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append(SIZE - r).append(" ");
            for (int c = 0; c < SIZE; c++) {
                sb.append(grid[r][c].getSymbol()).append(" ");
            }
            sb.append(SIZE - r).append("\n");
        }
        sb.append("  a b c d e f g h i\n");
        return sb.toString();
    }

    /**
     * Compara dos tableros por contenido
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        Board board = (Board) o;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] != board.grid[r][c]) return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                hash = 31 * hash + grid[r][c].getValue();
            }
        }
        return hash;
    }
}


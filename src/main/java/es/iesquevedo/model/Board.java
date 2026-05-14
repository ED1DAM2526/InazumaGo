package es.iesquevedo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Representa el tablero de Inazuma Go (9x9 intersecciones).
 * Estado de cada celda: vacía (0), negra (1), blanca (2).
 */
public class Board {
    private static final int SIZE = 9;
    private int[][] board; // 0 = empty, 1 = black, 2 = white

    public Board() {
        this.board = new int[SIZE][SIZE];
    }

    public Board(Board other) {
        this.board = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                this.board[r][c] = other.board[r][c];
            }
        }
    }

    /**
     * Obtiene el tamaño del tablero.
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * Obtiene el estado de una celda (0=vacía, 1=negra, 2=blanca).
     */
    public int getCell(int row, int col) {
        if (!isValid(row, col)) return -1;
        return board[row][col];
    }

    /**
     * Coloca una piedra en el tablero.
     */
    public void placeStone(int row, int col, int color) {
        if (isValid(row, col)) {
            board[row][col] = color;
        }
    }

    /**
     * Remueve una piedra del tablero.
     */
    public void removeStone(int row, int col) {
        if (isValid(row, col)) {
            board[row][col] = 0;
        }
    }

    /**
     * Verifica si una coordenada es válida.
     */
    public boolean isValid(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    /**
     * Verifica si una celda está vacía.
     */
    public boolean isEmpty(int row, int col) {
        return isValid(row, col) && board[row][col] == 0;
    }

    /**
     * Obtiene los vecinos ortogonales de una celda.
     */
    public List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int nr = row + dir[0];
            int nc = col + dir[1];
            if (isValid(nr, nc)) {
                neighbors.add(new int[]{nr, nc});
            }
        }
        return neighbors;
    }

    /**
     * Calcula las libertades (grados de libertad) de un grupo.
     * Retorna el número de intersecciones vacías adyacentes al grupo.
     */
    public int countLibertiesForGroup(int row, int col) {
        int color = board[row][col];
        if (color == 0) return 0;

        Set<String> visited = new HashSet<>();
        Set<String> liberties = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, col});
        visited.add(row + "," + col);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            for (int[] neighbor : getNeighbors(r, c)) {
                int nr = neighbor[0];
                int nc = neighbor[1];
                String key = nr + "," + nc;

                if (isEmpty(nr, nc)) {
                    liberties.add(key);
                } else if (board[nr][nc] == color && !visited.contains(key)) {
                    visited.add(key);
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        return liberties.size();
    }

    /**
     * Obtiene todas las piedras de un grupo.
     */
    public Set<String> getGroup(int row, int col) {
        int color = board[row][col];
        if (color == 0) return new HashSet<>();

        Set<String> group = new HashSet<>();
        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{row, col});
        group.add(row + "," + col);

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            for (int[] neighbor : getNeighbors(r, c)) {
                int nr = neighbor[0];
                int nc = neighbor[1];
                String key = nr + "," + nc;

                if (board[nr][nc] == color && !group.contains(key)) {
                    group.add(key);
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        return group;
    }

    /**
     * Detecta y captura grupos sin libertades.
     * Retorna el número de piedras capturadas.
     */
    public int captureGroupsWithoutLiberties() {
        int captured = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != 0 && countLibertiesForGroup(r, c) == 0) {
                    Set<String> group = getGroup(r, c);
                    for (String stone : group) {
                        String[] parts = stone.split(",");
                        int sr = Integer.parseInt(parts[0]);
                        int sc = Integer.parseInt(parts[1]);
                        removeStone(sr, sc);
                        captured++;
                    }
                }
            }
        }
        return captured;
    }

    /**
     * Crea una copia del tablero.
     */
    public Board clone() {
        return new Board(this);
    }

    /**
     * Compara dos tableros.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Board)) return false;
        Board other = (Board) obj;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (this.board[r][c] != other.board[r][c]) return false;
            }
        }
        return true;
    }

    /**
     * Representación en texto del tablero.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                char ch = board[r][c] == 0 ? '.' : (board[r][c] == 1 ? 'X' : 'O');
                sb.append(ch).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}


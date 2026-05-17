package es.iesquevedo.model;

public class BoardState {
    private final String[][] board;
    private final int size;

    public BoardState(int size) {
        this.size = size;
        this.board = new String[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = "";
            }
        }
    }

    public boolean makeMove(int row, int col, String playerId) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            return false;
        }
        if (!board[row][col].isEmpty()) {
            return false;
        }
        board[row][col] = playerId;
        return true;
    }

    public String checkWinner() {
        // Check rows
        for (int i = 0; i < size; i++) {
            if (!board[i][0].isEmpty() &&
                    board[i][0].equals(board[i][1]) &&
                    board[i][1].equals(board[i][2])) {
                return board[i][0];
            }
        }

        // Check columns
        for (int j = 0; j < size; j++) {
            if (!board[0][j].isEmpty() &&
                    board[0][j].equals(board[1][j]) &&
                    board[1][j].equals(board[2][j])) {
                return board[0][j];
            }
        }

        // Check diagonals
        if (!board[0][0].isEmpty() &&
                board[0][0].equals(board[1][1]) &&
                board[1][1].equals(board[2][2])) {
            return board[0][0];
        }

        if (!board[0][2].isEmpty() &&
                board[0][2].equals(board[1][1]) &&
                board[1][1].equals(board[2][0])) {
            return board[0][2];
        }

        // Check for draw
        boolean isFull = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j].isEmpty()) {
                    isFull = false;
                    break;
                }
            }
        }
        if (isFull) {
            return "DRAW";
        }

        return null; // Game continues
    }

    public String getCell(int row, int col) {
        return board[row][col];
    }

    public int getSize() {
        return size;
    }
}
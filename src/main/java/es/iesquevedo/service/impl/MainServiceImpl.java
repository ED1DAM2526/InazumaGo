package es.iesquevedo.service.impl;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.dto.Position;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.MainService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MainServiceImpl implements MainService {
    private final MainRepository repository; // puede ser null si no se usa

    // Estado de juego en memoria (3x3 Tic-Tac-Toe)
    private String[][] board;
    private String currentPlayer; // "X" o "O"
    private Optional<String> winner = Optional.empty();

    public MainServiceImpl() {
        this(null);
    }

    public MainServiceImpl(MainRepository repository) {
        this.repository = repository;
        startNewGame();
    }

    @Override
    public String greet() {
        if (this.repository == null) {
            return "Hello, InazumaGoPrevio!";
        }
        try {
            String name = null;
            try {
                name = repository.findDefaultName();
            } catch (Exception e) {
                // ignore
            }
            if (name == null || name.isEmpty()) {
                throw new es.iesquevedo.exception.NotFoundException("Default player name not found");
            }
            return "Hello, " + name + "!";
        } catch (es.iesquevedo.exception.NotFoundException nf) {
            throw nf;
        } catch (Exception e) {
            throw new RuntimeException("Error getting default name", e);
        }
    }

    @Override
    public void startNewGame() {
        board = new String[3][3];
        currentPlayer = "X";
        winner = Optional.empty();
    }

    @Override
    public boolean makeMove(Position position) {
        if (winner.isPresent()) {
            throw new RuntimeException("La partida ya finalizó");
        }
        int r = position.getRow();
        int c = position.getCol();
        if (r < 0 || r >= board.length || c < 0 || c >= board.length) {
            throw new RuntimeException("Posición fuera de rango");
        }
        if (board[r][c] != null && !board[r][c].isEmpty()) {
            throw new RuntimeException("Celda ocupada");
        }
        board[r][c] = currentPlayer;
        // comprobar ganador
        if (checkWinner(currentPlayer)) {
            winner = Optional.of(currentPlayer);
        } else if (isBoardFull()) {
            winner = Optional.of("EMPATE");
        } else {
            currentPlayer = currentPlayer.equals("X") ? "O" : "X";
        }
        return true;
    }

    @Override
    public String[][] getBoard() {
        // devolver copia defensiva
        String[][] copy = new String[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board.length);
        }
        return copy;
    }

    @Override
    public String getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public Optional<String> getWinner() {
        return winner;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board.length; c++) {
                if (board[r][c] == null || board[r][c].isEmpty()) return false;
            }
        }
        return true;
    }

    private boolean checkWinner(String player) {
        // filas
        for (int r = 0; r < 3; r++) {
            if (player.equals(board[r][0]) && player.equals(board[r][1]) && player.equals(board[r][2])) return true;
        }
        // columnas
        for (int c = 0; c < 3; c++) {
            if (player.equals(board[0][c]) && player.equals(board[1][c]) && player.equals(board[2][c])) return true;
        }
        // diagonales
        if (player.equals(board[0][0]) && player.equals(board[1][1]) && player.equals(board[2][2])) return true;
        if (player.equals(board[0][2]) && player.equals(board[1][1]) && player.equals(board[2][0])) return true;
        return false;
    }

    // ...existing code for Firebase or async methods kept as-is to preserve compatibility...

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        // placeholder: no-op local
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return null;
    }
}

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
        String name = repository.findDefaultName();
        if (name == null || name.trim().isEmpty()) {
            throw new NotFoundException("Default player name not found");
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

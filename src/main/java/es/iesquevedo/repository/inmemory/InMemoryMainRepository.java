package es.iesquevedo.repository.inmemory;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.model.BoardState;
import es.iesquevedo.repository.MainRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InMemoryMainRepository implements MainRepository {

    private final Map<String, GameStateDto> games = new HashMap<>();
    private final Map<String, BoardState> boards = new HashMap<>();
    private final Map<String, String> gameOwners = new HashMap<>();
    private int nextGameId = 1;

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        GameDto game = new GameDto(gameId, "InMemory", List.of(), "active", System.currentTimeMillis());
        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return "mock-listener-id";
    }

    @Override
    public void removeMovesListener(String gameId, String listenerId) {
    }

    @Override
    public String findDefaultName() {
        return "InazumaGoPrevio";
    }

    @Override
    public CompletableFuture<String> createGame(String playerId) {
        String gameId = "game-" + (nextGameId++);

        BoardState board = new BoardState(3);
        boards.put(gameId, board);

        GameStateDto game = new GameStateDto(
                gameId,
                new ArrayList<>(List.of(playerId)),
                playerId,
                "WAITING"
        );
        game.setBoardState(board);
        games.put(gameId, game);
        gameOwners.put(gameId, playerId);

        return CompletableFuture.completedFuture(gameId);
    }

    @Override
    public CompletableFuture<GameStateDto> joinGame(String gameId, String playerId) {
        GameStateDto game = games.get(gameId);
        if (game == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Game not found: " + gameId));
        }

        if (game.getPlayers().size() >= 2) {
            return CompletableFuture.failedFuture(new RuntimeException("Game is full"));
        }

        List<String> players = new ArrayList<>(game.getPlayers());
        players.add(playerId);
        game.setPlayers(players);
        game.setStatus("IN_PROGRESS");

        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<GameStateDto> submitMove(String gameId, String playerId, MovePayload payload, String clientNonce) {
        GameStateDto game = games.get(gameId);
        if (game == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Game not found: " + gameId));
        }

        // Validate game is in progress
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            return CompletableFuture.failedFuture(new RuntimeException("Game is not in progress"));
        }

        // Validate turn
        if (!game.getCurrentTurnPlayerId().equals(playerId)) {
            return CompletableFuture.failedFuture(new RuntimeException("Not your turn"));
        }

        // Extract move coordinates from payload
        int row = payload.getRow();
        int col = payload.getCol();

        BoardState board = boards.get(gameId);
        if (board == null) {
            board = new BoardState(3);
            boards.put(gameId, board);
        }

        // Make the move
        boolean moveMade = board.makeMove(row, col, playerId);
        if (!moveMade) {
            return CompletableFuture.failedFuture(new RuntimeException("Invalid move: cell occupied or out of bounds"));
        }

        // Check win condition
        String winner = board.checkWinner();
        if (winner != null) {
            if ("DRAW".equals(winner)) {
                game.setStatus("FINISHED");
                game.setWinnerId(null);
            } else {
                game.setStatus("FINISHED");
                game.setWinnerId(winner);
            }
        } else {
            // Change turn to other player
            List<String> players = game.getPlayers();
            if (players.size() >= 2) {
                String nextPlayer = players.get(0).equals(playerId) ? players.get(1) : players.get(0);
                game.setCurrentTurnPlayerId(nextPlayer);
            }
        }

        game.setBoardState(board);

        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<GameStateDto> getGameState(String gameId) {
        GameStateDto game = games.get(gameId);
        if (game == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Game not found: " + gameId));
        }
        return CompletableFuture.completedFuture(game);
    }
}
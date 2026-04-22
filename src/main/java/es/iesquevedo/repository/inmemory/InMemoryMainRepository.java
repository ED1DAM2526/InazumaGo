package es.iesquevedo.repository.inmemory;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.repository.MainRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class InMemoryMainRepository implements MainRepository {
    private final Map<String, GameDto> gamesStore = new ConcurrentHashMap<>();
    private final Map<String, List<Consumer<List<MoveData>>>> listeners = new ConcurrentHashMap<>();
    private final long simulatedLatencyMs;
    private int listenerIdCounter = 0;

    public InMemoryMainRepository() {
        this(50);
    }

    public InMemoryMainRepository(long simulatedLatencyMs) {
        this.simulatedLatencyMs = simulatedLatencyMs;
        initializeSampleData();
    }

    private void initializeSampleData() {
        GameDto sampleGame = new GameDto(
            "game-001",
            "Sample Match",
            Arrays.asList("player1", "player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );
        sampleGame.setMoves(new ArrayList<>());
        gamesStore.put("game-001", sampleGame);
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(simulatedLatencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            GameDto game = gamesStore.get(gameId);
            if (game != null) {
                GameDto copy = new GameDto(
                    game.getId(),
                    game.getName(),
                    new ArrayList<>(game.getPlayers()),
                    game.getStatus(),
                    game.getCreatedAt()
                );
                copy.setMoves(new ArrayList<>(game.getMoves() != null ? game.getMoves() : new ArrayList<>()));
                return copy;
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(simulatedLatencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            GameDto game = gamesStore.get(gameId);
            if (game == null) {
                throw new RuntimeException("Game not found: " + gameId);
            }

            // Añade los movimientos al juego
            if (game.getMoves() == null) {
                game.setMoves(new ArrayList<>());
            }
            game.getMoves().addAll(payload.getMoves());

            notifyListeners(gameId, game.getMoves());
        });
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        String listenerId = "listener-" + (++listenerIdCounter);

        listeners.computeIfAbsent(gameId, k -> new ArrayList<>()).add(listener);

        GameDto game = gamesStore.get(gameId);
        if (game != null && game.getMoves() != null) {
            listener.accept(new ArrayList<>(game.getMoves()));
        }

        return listenerId;
    }

    private void notifyListeners(String gameId, List<MoveData> moves) {
        List<Consumer<List<MoveData>>> listenersList = listeners.get(gameId);
        if (listenersList != null) {
            for (Consumer<List<MoveData>> listener : listenersList) {
                listener.accept(new ArrayList<>(moves));
            }
        }
    }

    public void clear() {
        gamesStore.clear();
        listeners.clear();
    }

    @Override
    public String findDefaultName() {
        return "InazumaGoPrevio!";
    }
}

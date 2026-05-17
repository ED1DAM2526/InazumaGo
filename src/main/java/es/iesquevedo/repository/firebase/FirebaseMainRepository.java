package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.repository.MainRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirebaseMainRepository implements MainRepository {
    private final String firebaseUrl;

    public FirebaseMainRepository(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return "firebase-listener-" + System.currentTimeMillis();
    }

    @Override
    public void removeMovesListener(String gameId, String listenerId) {
    }

    @Override
    public String findDefaultName() {
        return "FirebasePlayer";
    }

    // Método para pruebas con WireMock (no está en la interfaz)
    public boolean patchMultiPath(String path, Map<String, Object> updates) throws IOException {
        // TODO: Implementar llamada HTTP real con OkHttp
        return true;
    }

    // ========== NUEVOS MÉTODOS PARA E3-US3 ==========

    @Override
    public CompletableFuture<String> createGame(String playerId) {
        return CompletableFuture.completedFuture("firebase-game-" + System.currentTimeMillis());
    }

    @Override
    public CompletableFuture<GameStateDto> joinGame(String gameId, String playerId) {
        GameStateDto game = new GameStateDto();
        game.setGameId(gameId);
        game.setStatus("IN_PROGRESS");
        game.setPlayers(List.of(playerId));
        game.setCurrentTurnPlayerId(playerId);
        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<GameStateDto> submitMove(String gameId, String playerId, MovePayload payload, String clientNonce) {
        GameStateDto game = new GameStateDto();
        game.setGameId(gameId);
        game.setStatus("IN_PROGRESS");
        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<GameStateDto> getGameState(String gameId) {
        GameStateDto game = new GameStateDto();
        game.setGameId(gameId);
        game.setStatus("WAITING");
        return CompletableFuture.completedFuture(game);
    }
}

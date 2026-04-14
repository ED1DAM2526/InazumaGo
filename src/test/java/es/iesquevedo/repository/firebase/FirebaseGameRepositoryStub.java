package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Stub ligero para FirebaseGameRepository, usado en pruebas de integración.
 * Simula respuestas HTTP (200 para éxito, 403 para errores) y acepta configuración de endpoint/timeout.
 */
public class FirebaseGameRepositoryStub implements FirebaseGameRepository {

    private final String endpoint;
    private final int timeout;

    public FirebaseGameRepositoryStub(String endpoint, int timeout) {
        this.endpoint = endpoint;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        // Simula respuesta 200: devuelve un GameDto simulado
        GameDto simulatedGame = new GameDto("simulated-" + gameId, "Simulated Game", List.of("player1", "player2"), "IN_PROGRESS", System.currentTimeMillis());
        return CompletableFuture.completedFuture(simulatedGame);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        // Simula lógica: si gameId contiene "invalid", lanza excepción para simular 403
        if (gameId != null && gameId.contains("invalid")) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(new RuntimeException("Simulated 403: Invalid game"));
            return failed;
        }
        // Simula respuesta 200: éxito
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        // Simula listener: dispara un evento falso después de un delay simulado
        String listenerId = "listener-" + gameId + "-" + System.currentTimeMillis();
        new Thread(() -> {
            try {
                Thread.sleep(100); // Simula delay
                List<MoveData> simulatedMoves = List.of(
                    new MoveData("player1", "KICK", null),
                    new MoveData("player2", "PASS", null)
                );
                listener.accept(simulatedMoves);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return listenerId;
    }
}

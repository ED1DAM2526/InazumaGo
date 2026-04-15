package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;

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
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(timeout); // Simula timeout
                if (endpoint.contains("error")) {
                    return null; // Simula 404 o error
                }
                // Simula respuesta 200: devuelve un GameDto simulado
                GameDto simulatedGame = new GameDto("simulated-" + gameId, "Simulated Game", List.of("player1", "player2"), "IN_PROGRESS", System.currentTimeMillis());
                return simulatedGame;
            } catch (InterruptedException e) {
                throw new RuntimeException("Simulated timeout");
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(timeout); // Simula timeout
                if (endpoint.contains("error") || (gameId != null && gameId.contains("invalid"))) {
                    throw new RuntimeException("Simulated 403: Invalid game or endpoint error");
                }
                // Simula respuesta 200: éxito
            } catch (InterruptedException e) {
                throw new RuntimeException("Simulated timeout");
            }
        });
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        // Simula listener: dispara un evento falso después de un delay simulado usando timeout
        String listenerId = "listener-" + gameId + "-" + System.currentTimeMillis();
        new Thread(() -> {
            try {
                Thread.sleep(timeout); // Usa timeout para delay
                if (!endpoint.contains("error")) {
                    List<MoveData> simulatedMoves = List.of(
                        new MoveData("player1", "KICK", null),
                        new MoveData("player2", "PASS", null)
                    );
                    listener.accept(simulatedMoves);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        return listenerId;
    }

    @Override
    public String findDefaultName() {
        return "StubPlayer";
    }
}

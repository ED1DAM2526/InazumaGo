package es.iesquevedo.repository.inmemory;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implementación en memoria del repositorio. Simula respuestas sin conectar a Firebase.
 * Útil para tests unitarios y desarrollo local.
 *
 * Características:
 * - Simula latencia configurable
 * - Almacena datos en mapas (HashMap)
 * - Soporta listeners de cambios
 * - Se resetea cada ejecución (no persiste)
 */
public class InMemoryMainRepository implements MainRepository {

    private final Map<String, GameDto> gamesStore = new ConcurrentHashMap<>();
    private final Map<String, List<Consumer<List<MoveData>>>> listeners = new ConcurrentHashMap<>();
    private final long simulatedLatencyMs;
    private int listenerIdCounter = 0;

    /**
     * Constructor sin argumentos. Usa latencia por defecto de 50ms.
     */
    public InMemoryMainRepository() {
        this(50);
    }

    /**
     * Constructor que acepta configuración de latencia.
     *
     * @param simulatedLatencyMs milisegundos de latencia a simular
     */
    public InMemoryMainRepository(long simulatedLatencyMs) {
        this.simulatedLatencyMs = simulatedLatencyMs;
        initializeSampleData();
    }

    /**
     * Inicializa datos de ejemplo para pruebas locales.
     */
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
        // Simula latencia de red
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(simulatedLatencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            GameDto game = gamesStore.get(gameId);
            if (game != null) {
                // Devolver una copia para evitar mutaciones externas
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
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        // Simula una escritura PATCH
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(simulatedLatencyMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            GameDto game = gamesStore.get(gameId);
            if (game == null) {
                throw new NotFoundException("Game not found: " + gameId);
            }

            // Añade los movimientos al juego
            if (game.getMoves() == null) {
                game.setMoves(new ArrayList<>());
            }
            game.getMoves().addAll(payload.getMoves());

            // Notifica a los listeners
            notifyListeners(gameId, game.getMoves());
        });
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        String listenerId = "listener-" + (++listenerIdCounter);

        listeners.computeIfAbsent(gameId, k -> new ArrayList<>())
                .add(listener);

        // Notifica inmediatamente con los datos actuales
        GameDto game = gamesStore.get(gameId);
        if (game != null && game.getMoves() != null) {
            listener.accept(new ArrayList<>(game.getMoves()));
        }

        return listenerId;
    }

    @Override
    public void removeMovesListener(String gameId, String listenerId) {
        // En esta implementación simple, removemos todos los listeners.
        // Una implementación real mantendría un registro de IDs.
        listeners.remove(gameId);
    }

    /**
     * Notifica a todos los listeners de un gameId.
     */
    private void notifyListeners(String gameId, List<MoveData> moves) {
        List<Consumer<List<MoveData>>> listenersList = listeners.get(gameId);
        if (listenersList != null) {
            for (Consumer<List<MoveData>> listener : listenersList) {
                listener.accept(new ArrayList<>(moves));
            }
        }
    }

    /**
     * Limpia el almacén (útil para tests).
     */
    public void clear() {
        gamesStore.clear();
        listeners.clear();
    }

    @Override
    public String findDefaultName() { return "InazumaGoPrevio"; }
}

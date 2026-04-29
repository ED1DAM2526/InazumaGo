package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio especializado para la sincronización de eventos de partida.
 * Maneja la grabación de eventos de inicio, movimiento y fin de partida en Firebase Realtime Database.
 */
public class GameEventRepository {
    private final FirebaseDatabase database;
    private static final String EVENTS_PATH = "game_events";

    /**
     * Tipos de eventos soportados
     */
    public enum EventType {
        GAME_START("game.start"),
        GAME_MOVE("game.move"),
        GAME_END("game.end");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Constructor con inyección de FirebaseDatabase (para tests)
     */
    public GameEventRepository(FirebaseDatabase database) {
        this.database = database;
    }

    /**
     * Constructor con URL de Firebase
     */
    public GameEventRepository(String firebaseUrl) {
        this.database = FirebaseDatabase.getInstance(firebaseUrl);
    }

    /**
     * Registra el evento de inicio de partida
     *
     * @param gameId ID de la partida
     * @param gameDto Datos de la partida
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    public CompletableFuture<Void> recordGameStart(String gameId, GameDto gameDto) {
        return recordEvent(EventType.GAME_START, gameId, gameDto);
    }

    /**
     * Registra un movimiento de jugador durante la partida
     *
     * @param gameId ID de la partida
     * @param moveData Datos del movimiento
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    public CompletableFuture<Void> recordGameMove(String gameId, MoveData moveData) {
        return recordEvent(EventType.GAME_MOVE, gameId, moveData);
    }

    /**
     * Registra el evento de fin de partida
     *
     * @param gameId ID de la partida
     * @param gameDto Datos finales de la partida
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    public CompletableFuture<Void> recordGameEnd(String gameId, GameDto gameDto) {
        return recordEvent(EventType.GAME_END, gameId, gameDto);
    }

    /**
     * Registra un evento genérico en Firebase
     *
     * @param eventType Tipo de evento (inicio, movimiento, fin)
     * @param gameId ID de la partida
     * @param payload Datos del evento
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    private CompletableFuture<Void> recordEvent(EventType eventType, String gameId, Object payload) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            DatabaseReference eventsRef = database.getReference(EVENTS_PATH);

            // Crear el objeto de evento con metadatos
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("type", eventType.getValue());
            eventData.put("gameId", gameId);
            eventData.put("timestamp", System.currentTimeMillis());
            eventData.put("payload", payload);

            // Usar push para generar ID único automáticamente
            eventsRef.push().setValue(eventData, (error, ref) -> {
                if (error != null) {
                    future.completeExceptionally(
                        new Exception("Error al grabar evento " + eventType.getValue() + ": " + error.getMessage())
                    );
                } else {
                    future.complete(null);
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    /**
     * Obtiene la referencia de base de datos para una partida
     * Útil para obtener acceso directo a eventos de una partida específica
     *
     * @param gameId ID de la partida
     * @return DatabaseReference para los eventos de la partida
     */
    public DatabaseReference getGameEventsReference(String gameId) {
        return database.getReference(EVENTS_PATH)
            .orderByChild("gameId")
            .equalTo(gameId);
    }

}


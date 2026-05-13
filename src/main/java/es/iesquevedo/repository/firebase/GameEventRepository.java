package es.iesquevedo.repository.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.GameEventDto;
import es.iesquevedo.dto.MoveData;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio especializado para la sincronización de eventos de partida.
 * Puede operar en dos modos:
 * 1) Firebase SDK, cuando se inyecta {@link FirebaseDatabase}.
 * 2) HTTP REST, cuando se construye con una base URL.
 */
public class GameEventRepository {
    private static final String EVENTS_PATH = "game_events";
    private static final String FIELD_GAME_ID = "gameId";

    private final FirebaseDatabase database;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    /**
     * Tipos de eventos soportados.
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
     * Constructor con inyección de FirebaseDatabase (para tests de SDK).
     */
    public GameEventRepository(FirebaseDatabase database) {
        this.database = database;
        this.baseUrl = null;
        this.httpClient = null;
    }

    /**
     * Constructor con base URL REST compatible con WireMock/Firebase REST.
     */
    public GameEventRepository(String firebaseUrl) {
        this.database = null;
        this.baseUrl = normalizeBaseUrl(firebaseUrl);
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Registra el evento de inicio de partida.
     */
    public CompletableFuture<Void> recordGameStart(String gameId, GameDto gameDto) {
        return recordEvent(EventType.GAME_START, gameId, gameDto);
    }

    /**
     * Registra un movimiento de jugador durante la partida.
     */
    public CompletableFuture<Void> recordGameMove(String gameId, MoveData moveData) {
        return recordEvent(EventType.GAME_MOVE, gameId, moveData);
    }

    /**
     * Registra el evento de fin de partida.
     */
    public CompletableFuture<Void> recordGameEnd(String gameId, GameDto gameDto) {
        return recordEvent(EventType.GAME_END, gameId, gameDto);
    }

    /**
     * Recupera los eventos de una partida.
     */
    public CompletableFuture<List<GameEventDto>> getGameEvents(String gameId) {
        if (baseUrl != null) {
            return fetchGameEventsHttp(gameId);
        }
        return fetchGameEventsFirebase(gameId);
    }

    /**
     * Obtiene la referencia de base de datos para una partida.
     */
    public Query getGameEventsReference(String gameId) {
        if (database == null) {
            throw new IllegalStateException("getGameEventsReference solo está disponible en modo Firebase SDK");
        }
        return database.getReference(EVENTS_PATH)
            .orderByChild(FIELD_GAME_ID)
            .equalTo(gameId);
    }

    private CompletableFuture<Void> recordEvent(EventType eventType, String gameId, Object payload) {
        if (baseUrl != null) {
            return recordEventHttp(eventType, gameId, payload);
        }
        return recordEventFirebase(eventType, gameId, payload);
    }

    private CompletableFuture<Void> recordEventHttp(EventType eventType, String gameId, Object payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> eventData = buildEventData(eventType, gameId, payload);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/events/" + eventType.getValue()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(eventData)))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return;
                }
                throw new IllegalStateException(
                    "Error al grabar evento " + eventType.getValue() +
                        " (HTTP " + response.statusCode() + "): " + response.body()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Hilo interrumpido al grabar evento " + eventType.getValue(), e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Error al grabar evento " + eventType.getValue(), e);
            }
        });
    }

    private CompletableFuture<Void> recordEventFirebase(EventType eventType, String gameId, Object payload) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            DatabaseReference eventsRef = database.getReference(EVENTS_PATH);
            Map<String, Object> eventData = buildEventData(eventType, gameId, payload);
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

    private CompletableFuture<List<GameEventDto>> fetchGameEventsHttp(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedGameId = URLEncoder.encode(gameId, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/events?gameId=" + encodedGameId))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    GameEventDto[] events = gson.fromJson(response.body(), GameEventDto[].class);
                    return events == null ? List.of() : Arrays.asList(events);
                }
                throw new IllegalStateException(
                    "Error al recuperar eventos (HTTP " + response.statusCode() + "): " + response.body()
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Hilo interrumpido al recuperar eventos", e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Error al recuperar eventos", e);
            }
        });
    }

    private CompletableFuture<List<GameEventDto>> fetchGameEventsFirebase(String gameId) {
        CompletableFuture<List<GameEventDto>> future = new CompletableFuture<>();
        try {
            Query query = database.getReference(EVENTS_PATH)
                .orderByChild(FIELD_GAME_ID)
                .equalTo(gameId);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<GameEventDto> events = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        GameEventDto event = child.getValue(GameEventDto.class);
                        if (event != null) {
                            if (event.getId() == null) {
                                event.setId(child.getKey());
                            }
                            events.add(event);
                        }
                    }
                    future.complete(events);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(new Exception(error.getMessage()));
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private Map<String, Object> buildEventData(EventType eventType, String gameId, Object payload) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("type", eventType.getValue());
        eventData.put(FIELD_GAME_ID, gameId);
        eventData.put("timestamp", System.currentTimeMillis());
        eventData.put("payload", payload);
        return eventData;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La base URL no puede ser nula o vacía ");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}

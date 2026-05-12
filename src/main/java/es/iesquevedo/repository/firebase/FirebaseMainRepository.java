package es.iesquevedo.repository.firebase;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.repository.MainRepository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repositorio para Firebase Realtime Database con cliente HTTP (OkHttp).
 * Soporta CRUD básico, escritura multi-path (PATCH) y listeners simulados.
 */
public class FirebaseMainRepository implements MainRepository {
    private static final Logger LOGGER = Logger.getLogger(FirebaseMainRepository.class.getName());
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String SUFFIX = ".json";

    private final String firebaseUrl;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Map<String, Consumer<List<MoveData>>> movesListeners;
    private final int timeoutSeconds;
    private String idToken; // Token de autenticación

    /**
     * Constructor con URL de Firebase (sin .firebaseio.com, se añade automáticamente).
     */
    public FirebaseMainRepository(String firebaseUrl) {
        this(firebaseUrl, 30);
    }

    /**
     * Constructor con URL y timeout configurables.
     */
    public FirebaseMainRepository(String firebaseUrl, int timeoutSeconds) {
        this.firebaseUrl = normalizeUrl(firebaseUrl);
        this.timeoutSeconds = timeoutSeconds;
        this.gson = new Gson();
        this.movesListeners = new ConcurrentHashMap<>();
        this.httpClient = createHttpClient();
        this.idToken = null; // Se establece después de autenticar
    }

    /**
     * Normaliza la URL de Firebase (garantiza formato correcto).
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "https://localhost:9000"; // Fallback para tests con WireMock
        }
        url = url.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (!url.endsWith(".firebaseio.com") && !url.endsWith(".com")) {
            url = url + ".firebaseio.com";
        }
        return url;
    }

    /**
     * Crea cliente HTTP con timeout y configuración.
     */
    private OkHttpClient createHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .readTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .writeTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Establece el token de autenticación para futuras peticiones.
     */
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    /**
     * Obtiene el token de autenticación actual.
     */
    public String getCurrentToken() {
        return this.idToken;
    }

    @Override
    public CompletableFuture<GameDto> createGame(GameDto game) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = firebaseUrl + "/games/" + game.getId() + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }
                String json = gson.toJson(game);
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        GameDto created = gson.fromJson(response.body().string(), GameDto.class);
                        LOGGER.log(Level.INFO, "Game creado: " + game.getId());
                        return created;
                    } else {
                        LOGGER.log(Level.WARNING, "Error al crear game: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en createGame: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<GameDto>> listGames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = firebaseUrl + "/games" + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Firebase devuelve Map<String, GameDto> o null
                        String body = response.body().string();
                        if ("null".equals(body)) {
                            return List.of();
                        }
                        Map<String, GameDto> games = gson.fromJson(body,
                                new com.google.gson.reflect.TypeToken<Map<String, GameDto>>() {}.getType());
                        return games != null ? List.copyOf(games.values()) : List.of();
                    } else {
                        LOGGER.log(Level.WARNING, "Error al listar games: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en listGames: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        if ("null".equals(body)) {
                            return null;
                        }
                        GameDto gameDto = gson.fromJson(body, GameDto.class);
                        LOGGER.log(Level.INFO, "Game obtenido: " + gameId);
                        return gameDto;
                    } else if (response.code() == 404) {
                        return null;
                    } else {
                        LOGGER.log(Level.WARNING, "Error al obtener game: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en getGame: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<GameDto> updateGame(String gameId, GameDto game) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }
                String json = gson.toJson(game);
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        GameDto updated = gson.fromJson(response.body().string(), GameDto.class);
                        LOGGER.log(Level.INFO, "Game actualizado: " + gameId);
                        return updated;
                    } else {
                        LOGGER.log(Level.WARNING, "Error al actualizar game: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en updateGame: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteGame(String gameId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        LOGGER.log(Level.WARNING, "Error al eliminar game: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                    LOGGER.log(Level.INFO, "Game eliminado: " + gameId);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en deleteGame: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Construir URL para PATCH multi-path
                String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                if (idToken != null) {
                    url += "?auth=" + idToken;
                }

                // Crear payload con estructura PATCH
                Map<String, Object> updates = new HashMap<>();
                updates.put("moves", payload.getMoves());
                updates.put("timestamp", payload.getTimestamp());
                updates.put("gameVersion", payload.getGameVersion());

                String json = gson.toJson(updates);
                RequestBody body = RequestBody.create(json, JSON);
                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.code() == 403) {
                        throw new IOException("Movimiento rechazado por reglas del servidor (403)");
                    }
                    if (!response.isSuccessful()) {
                        LOGGER.log(Level.WARNING, "Error al escribir movimientos: " + response.code());
                        throw new IOException("HTTP " + response.code());
                    }
                    LOGGER.log(Level.INFO, "Movimientos guardados para partida: " + gameId);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error en writeMoveMultiPath: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        // Simulación básica: guardar listener en mapa para tests
        // En producción, implementar SSE (Server-Sent Events) o WebSocket
        String listenerId = "listener-" + UUID.randomUUID();
        movesListeners.put(listenerId, listener);
        LOGGER.log(Level.INFO, "Listener añadido para partida: " + gameId);
        return listenerId;
    }

    public void removeMovesListener(String gameId, String listenerId) {
        movesListeners.remove(listenerId);
        LOGGER.log(Level.INFO, "Listener removido: " + listenerId);
    }

    @Override
    public String findDefaultName() {
        return "FirebasePlayer";
    }

    /**
     * Método auxiliar para notificar a listeners (uso interno en tests).
     */
    protected void notifyMovesListeners(List<MoveData> moves) {
        movesListeners.values().forEach(listener -> listener.accept(moves));
    }
}
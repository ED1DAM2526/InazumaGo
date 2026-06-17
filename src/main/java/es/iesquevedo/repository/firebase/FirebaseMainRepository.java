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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private final Map<String, SSEListener> activeSSEListeners; // SSE listeners activos
    private final ScheduledExecutorService reconnectExecutor; // Pool para reconexiones
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
        this.activeSSEListeners = new ConcurrentHashMap<>();
        this.reconnectExecutor = Executors.newScheduledThreadPool(2);
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

    private void logRequestUrl(String operation, Request request) {
        System.out.println("URL de la petición " + operation + ": " + request.url());
    }

    @Override
    public CompletableFuture<GameDto> createGame(GameDto game) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = firebaseUrl + "/games/" + game.getId() + SUFFIX;
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in createGame!");
                }
                
                String json = gson.toJson(game);
                RequestBody body = RequestBody.create(json, JSON);
                
                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
                        .build();

                logRequestUrl("createGame", request);

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        GameDto created = gson.fromJson(response.body().string(), GameDto.class);
                        LOGGER.log(Level.INFO, "Game creado: " + game.getId());
                        return created;
                    } else {
                        LOGGER.log(Level.WARNING, "Error al crear game: " + response.code());
                        if (response.body() != null) {
                            LOGGER.log(Level.WARNING, "Response: " + response.body().string());
                        }
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
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in listGames!");
                }
                
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                logRequestUrl("listGames", request);

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String body = response.body().string();
                        if ("null".equals(body) || body.isEmpty()) {
                            return List.of();
                        }
                        
                        try {
                            Map<String, GameDto> games = gson.fromJson(body,
                                    new com.google.gson.reflect.TypeToken<Map<String, GameDto>>() {}.getType());
                            return games != null ? List.copyOf(games.values()) : List.of();
                        } catch (com.google.gson.JsonSyntaxException e) {
                            LOGGER.log(Level.WARNING, "Respuesta no es JSON válido: " + body.substring(0, Math.min(100, body.length())));
                            return List.of();
                        }
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
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in getGame!");
                }
                
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .build();

                logRequestUrl("getGame", request);

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
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in updateGame!");
                }
                
                String json = gson.toJson(game);
                RequestBody body = RequestBody.create(json, JSON);
                
                Request request = new Request.Builder()
                        .url(url)
                        .patch(body)
                        .build();

                logRequestUrl("updateGame", request);

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
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in deleteGame!");
                }
                
                Request request = new Request.Builder()
                        .url(url)
                        .delete()
                        .build();

                logRequestUrl("deleteGame", request);

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
                if (idToken != null && !idToken.isEmpty()) {
                    url += "?auth=" + idToken;
                } else {
                    LOGGER.log(Level.WARNING, "⚠️ idToken is NULL or empty in writeMoveMultiPath!");
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

                logRequestUrl("writeMoveMultiPath", request);

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

    /**
     * Abre un listener SSE (Server-Sent Events) real para cambios en tiempo real.
     * Implementa reconexión automática con backoff exponencial.
     * 
     * @param gameId ID de la partida
     * @param listener callback que se ejecuta cuando hay cambios
     * @return ID del listener para posterior cierre
     */
    public String openSSEListener(String gameId, Consumer<GameDto> listener) {
        String listenerId = "sse-" + UUID.randomUUID();
        SSEListener sseListener = new SSEListener(gameId, listener, listenerId);
        activeSSEListeners.put(listenerId, sseListener);
        
        // Inicia el listener de forma asíncrona
        CompletableFuture.runAsync(sseListener::connect);
        
        LOGGER.log(Level.INFO, "SSE Listener abierto para partida: " + gameId);
        return listenerId;
    }

    /**
     * Cierra un listener SSE.
     * 
     * @param listenerId ID del listener
     */
    public void closeSSEListener(String listenerId) {
        SSEListener listener = activeSSEListeners.remove(listenerId);
        if (listener != null) {
            listener.close();
            LOGGER.log(Level.INFO, "SSE Listener cerrado: " + listenerId);
        }
    }

    /**
     * Clase interna para manejar conexiones SSE con reconexión automática.
     */
    private class SSEListener {
        private final String gameId;
        private final Consumer<GameDto> listener;
        private final String listenerId;
        private Response response;
        private volatile boolean running;
        private int reconnectAttempts = 0;
        private static final int MAX_RECONNECT_ATTEMPTS = 10;
        private static final long INITIAL_BACKOFF_MS = 1000;
        private static final long MAX_BACKOFF_MS = 30000;

        SSEListener(String gameId, Consumer<GameDto> listener, String listenerId) {
            this.gameId = gameId;
            this.listener = listener;
            this.listenerId = listenerId;
            this.running = false;
        }

        void connect() {
            running = true;
            while (running && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                try {
                    String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                    if (idToken != null && !idToken.isEmpty()) {
                        url += "?auth=" + idToken;
                    }
                    
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    logRequestUrl("openSSEListener", request);

                    response = httpClient.newCall(request).execute();
                    
                    if (!response.isSuccessful()) {
                        handleConnectionError(response.code());
                        continue;
                    }

                    // Resetear contador de intentos al conectar exitosamente
                    reconnectAttempts = 0;
                    
                    // Poll en lugar de SSE real (SSE requiere servidor con streaming)
                    // Para multiplayer real, hacer polling cada 500ms
                    pollGameUpdates();

                } catch (IOException e) {
                    if (running) {
                        handleConnectionError(0);
                    }
                } finally {
                    if (response != null && response.body() != null) {
                        response.body().close();
                    }
                }
            }
            
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                LOGGER.log(Level.WARNING, "SSE Listener alcanzó máximo de intentos de reconexión: " + listenerId);
            }
        }

        void pollGameUpdates() throws IOException {
            // Polling simple con intervalo
            long lastUpdate = System.currentTimeMillis();
            
            while (running && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                try {
                    // Poll cada 500ms (puede ajustarse)
                    Thread.sleep(500);
                    
                    // Realizar GET para obtener estado actual
                    String url = firebaseUrl + "/games/" + gameId + SUFFIX;
                    if (idToken != null && !idToken.isEmpty()) {
                        url += "?auth=" + idToken;
                    }
                    
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();

                    logRequestUrl("pollGameUpdates", request);

                    try (Response pollResponse = httpClient.newCall(request).execute()) {
                        if (pollResponse.isSuccessful() && pollResponse.body() != null) {
                            String body = pollResponse.body().string();
                            if (!"null".equals(body)) {
                                GameDto gameDto = gson.fromJson(body, GameDto.class);
                                listener.accept(gameDto);
                                lastUpdate = System.currentTimeMillis();
                            }
                        } else if (pollResponse.code() == 401) {
                            // Token expirado
                            LOGGER.log(Level.WARNING, "Token expirado en SSE listener");
                            break;
                        } else if (pollResponse.code() != 200) {
                            handleConnectionError(pollResponse.code());
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    handleConnectionError(0);
                    break;
                }
            }
        }

        void handleConnectionError(int httpCode) {
            if (!running) return;
            
            reconnectAttempts++;
            long backoffTime = Math.min(
                    INITIAL_BACKOFF_MS * (long) Math.pow(2, reconnectAttempts - 1),
                    MAX_BACKOFF_MS
            );
            
            LOGGER.log(Level.WARNING, 
                    "SSE conexión fallida (HTTP " + httpCode + "). Reconectando en " + backoffTime + "ms. Intento " + 
                    reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);
            
            try {
                Thread.sleep(backoffTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }

        void close() {
            running = false;
            if (response != null && response.body() != null) {
                response.body().close();
            }
            LOGGER.log(Level.INFO, "SSE Listener cerrado gracefully");
        }
    }

    /**
     * Limpia todos los listeners SSE activos (usar en destructor o logout).
     */
    public void closeAllSSEListeners() {
        List<String> listenerIds = new java.util.ArrayList<>(activeSSEListeners.keySet());
        for (String id : listenerIds) {
            closeSSEListener(id);
        }
        recreateReconnectExecutor();
        LOGGER.log(Level.INFO, "Todos los SSE listeners cerrados");
    }

    private void recreateReconnectExecutor() {
        if (reconnectExecutor.isShutdown()) {
            // No es posible reutilizar executor cerrado, mantener el existente
            // En una aplicación real, considerar usar una nueva instancia
        }
    }
}
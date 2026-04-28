package es.iesquevedo.repository.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesquevedo.repository.MainRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class FirebaseMainRepository implements MainRepository {

    private final String endpoint;   // ej: https://mi-app.firebaseio.com
    private final int timeoutSeconds;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public FirebaseMainRepository(String endpoint, int timeoutSeconds) {
        this.endpoint = endpoint;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public void writeMovesMultiPath(Map<String, Object> updates, String idToken) {
        try {
            // Firebase multi-path PATCH: PATCH /.json?auth=TOKEN con el mapa completo
            String url = endpoint + "/.json?auth=" + idToken;
            String body = mapper.writeValueAsString(updates);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 403) {
                throw new RuntimeException("Firebase rechazó la operación: 403");
            }
            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error Firebase: " + response.statusCode());
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error en writeMovesMultiPath", e);
        }
    }

    @Override
    public Map<String, Object> getGame(String gameId, String idToken) {
        try {
            String url = endpoint + "/games/" + gameId + ".json?auth=" + idToken;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error Firebase: " + response.statusCode());
            }

            return mapper.readValue(response.body(), Map.class);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error en getGame", e);
        }
    }

    @Override
    public void addMovesListener(String path, MovesListener listener) {
        // SSE streaming — implementación básica
        // En producción usar OkHttp o similar para SSE real
        System.out.println("[Firebase] Listener registrado en: " + path);
    }
}

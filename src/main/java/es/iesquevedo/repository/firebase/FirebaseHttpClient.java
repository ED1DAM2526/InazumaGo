package es.iesquevedo.repository.firebase;

import es.iesquevedo.service.auth.AuthService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente HTTP minimo para llamadas a Firebase RTDB.
 */
public class FirebaseHttpClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final AuthService authService;

    public FirebaseHttpClient(String baseUrl, HttpClient httpClient, AuthService authService) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
        this.authService = authService;
    }

    public HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET();

        authService.getToken().ifPresent(token -> builder.header("Authorization", "Bearer " + token));

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}


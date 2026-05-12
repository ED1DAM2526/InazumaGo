package es.iesquevedo.repository.firebase;

import es.iesquevedo.service.auth.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Cliente HTTP minimo para llamadas a Firebase RTDB.
 */
public class FirebaseHttpClient {
    private final String baseUrl;
    private final AuthService authService;

    public FirebaseHttpClient(String baseUrl, AuthService authService) {
        this.baseUrl = baseUrl;
        this.authService = authService;
    }

    public HttpResponse<String> get(String path) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (authService.getToken().isPresent()) {
            connection.setRequestProperty("Authorization", "Bearer " + authService.getToken().get());
        }

        int statusCode = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        connection.disconnect();

        return new HttpResponse<>(response.toString(), statusCode);
    }

    /**
     * Clase simple para encapsular respuesta HTTP
     */
    public static class HttpResponse<T> {
        private final T body;
        private final int statusCode;

        public HttpResponse(T body) {
            this.body = body;
            this.statusCode = 200; // Default
        }

        public HttpResponse(T body, int statusCode) {
            this.body = body;
            this.statusCode = statusCode;
        }

        public T body() {
            return body;
        }

        public int statusCode() {
            return statusCode;
        }
    }
}


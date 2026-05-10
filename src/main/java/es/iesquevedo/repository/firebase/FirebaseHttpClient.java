package es.iesquevedo.repository.firebase;

import es.iesquevedo.service.auth.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        URLConnection connection = url.openConnection();

        if (authService.getToken().isPresent()) {
            connection.setRequestProperty("Authorization", "Bearer " + authService.getToken().get());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return new HttpResponse<>(response.toString());
    }

    /**
     * Clase simple para encapsular respuesta HTTP
     */
    public static class HttpResponse<T> {
        private final T body;

        public HttpResponse(T body) {
            this.body = body;
        }

        public T body() {
            return body;
        }
    }
}


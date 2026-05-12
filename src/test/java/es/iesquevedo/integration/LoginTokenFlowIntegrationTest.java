package es.iesquevedo.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import es.iesquevedo.repository.firebase.FirebaseHttpClient;
import es.iesquevedo.service.auth.AuthServiceMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTokenFlowIntegrationTest {

    private HttpServer server;
    private volatile String capturedAuthorization;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/games.json", new TokenCaptureHandler());
        server.start();

        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void loginSavesTokenAndFirebaseHttpClientIncludesItInNextCall() throws Exception {
        AuthServiceMock authService = new AuthServiceMock();
        String token = authService.login("qa@inazuma.local", "secret");

        assertTrue(authService.getToken().isPresent());
        assertEquals(token, authService.getToken().orElseThrow());

        FirebaseHttpClient firebaseHttpClient = new FirebaseHttpClient(baseUrl, HttpClient.newHttpClient(), authService);
        HttpResponse<String> response = firebaseHttpClient.get("/games.json");

        assertEquals(200, response.statusCode());
        assertEquals("Bearer " + token, capturedAuthorization);
    }

    private class TokenCaptureHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            capturedAuthorization = exchange.getRequestHeaders().getFirst("Authorization");
            byte[] response = "{}".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}


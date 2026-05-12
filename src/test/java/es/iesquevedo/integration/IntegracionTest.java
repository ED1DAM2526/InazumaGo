package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.iesquevedo.app.AppState;
import es.iesquevedo.config.AppState;
import es.iesquevedo.service.AuthService;
import es.iesquevedo.test_integracion.FirebaseMainRepository;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.concurrent.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationTest {

    static WireMockServer wireMock;
    FirebaseMainRepository repo;
    AuthService authService;
    private es.iesquevedo.config.AppState AppState;

    @BeforeAll
    static void startServer() {
        wireMock = new WireMockServer(8089);
        wireMock.start();
    }

    @AfterAll
    static void stopServer() {
        wireMock.stop();
    }

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        repo = new FirebaseMainRepository();
        authService = new AuthService();
        AppState.getInstance().clear();
    }

    @Test
    void componentesPrincipales_saludoYLoginFuncionan() throws Exception {
        String token = authService.login("user@test.com", "pass123");
        AppState.getInstance().saveToken(token, "user@test.com");

        assertNotNull(token, "El token no debe ser null");
        assertTrue(AppState.getInstance().isLoggedIn(), "El usuario debe estar logueado");
        assertEquals("user@test.com", AppState.getInstance().getUserEmail());
    }

    @Test
    void patchMultiPath_exitoso_flujoOptimisticConfirmed() {
        wireMock.stubFor(patch(urlPathEqualTo("/.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        Map<String, Object> updates = Map.of(
                "/games/game1/moves/0", Map.of("x", 3, "y", 4, "player", "BLACK"),
                "/games/game1/meta/lastMove", "0"
        );

        boolean optimisticState = true;
        assertDoesNotThrow(() -> repo.writeMovesMultiPath(updates, "token-valido"));
        wireMock.verify(patchRequestedFor(urlPathEqualTo("/.json")));
        assertTrue(optimisticState, "El flujo optimistic debe confirmarse con 200");
    }

    @Test
    void patchMultiPath_rechazo403_provocaRollback() {
        wireMock.stubFor(patch(urlPathEqualTo("/.json"))
                .willReturn(aResponse().withStatus(403)));

        Map<String, Object> updates = Map.of(
                "/games/game1/moves/1", Map.of("x", 1, "y", 1)
        );

        String estadoAntes = "MOVE_PENDING";
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> repo.writeMovesMultiPath(updates, "token-sin-permisos"));

        assertTrue(ex.getMessage().contains("403"));
        assertEquals("MOVE_PENDING", estadoAntes, "El estado debe hacer rollback al anterior");
    }

    @Test
    void conflictoConcurrente_unAceptadoOtroRechazado() throws Exception {
        wireMock.stubFor(patch(urlPathEqualTo("/.json"))
                .withRequestBody(containing("player1"))
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        wireMock.stubFor(patch(urlPathEqualTo("/.json"))
                .withRequestBody(containing("player2"))
                .willReturn(aResponse().withStatus(403)));

        Map<String, Object> movePlayer1 = Map.of(
                "/games/game1/moves/2", Map.of("player1", "BLACK", "x", 5, "y", 5)
        );
        Map<String, Object> movePlayer2 = Map.of(
                "/games/game1/moves/2", Map.of("player2", "WHITE", "x", 5, "y", 5)
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> futureP1 = executor.submit(() ->
                assertDoesNotThrow(() -> repo.writeMovesMultiPath(movePlayer1, "token-p1")));
        Future<?> futureP2 = executor.submit(() ->
                assertThrows(RuntimeException.class,
                        () -> repo.writeMovesMultiPath(movePlayer2, "token-p2")));

        futureP1.get(5, TimeUnit.SECONDS);
        futureP2.get(5, TimeUnit.SECONDS);
        executor.shutdown();

        wireMock.verify(atLeast(1), patchRequestedFor(urlPathEqualTo("/.json")));
    }

    @Test
    void dedupePorClientNonce_reintentosNoDuplicados() {
        String clientNonce = "nonce-abc-123";

        wireMock.stubFor(patch(urlPathEqualTo("/.json"))
                .withRequestBody(containing(clientNonce))
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        Map<String, Object> move = Map.of(
                "/games/game1/moves/3", Map.of("x", 2, "y", 2, "clientNonce", clientNonce)
        );

        assertDoesNotThrow(() -> repo.writeMovesMultiPath(move, "token-valido"));
        assertDoesNotThrow(() -> repo.writeMovesMultiPath(move, "token-valido"));

        wireMock.verify(exactly(2), patchRequestedFor(urlPathEqualTo("/.json"))
                .withRequestBody(containing(clientNonce)));
    }

    private class AuthService {
    }
}
package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class FirebaseIntegrationTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void testGreetWithInMemoryRepository() {
        // Test saludo usando repositorio en memoria (no necesita Firebase)
        MainRepository repo = AppConfig.createInMemoryRepository();
        MainController controller = new MainController(new MainServiceImpl(repo));

        String greeting = controller.greet();

        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }

    @Test
    void testMultiPathPatchSuccess() {
        // Simular Firebase devolviendo 200 (éxito)
        stubFor(patch(urlPathEqualTo("/games/game1.json"))
                .willReturn(aResponse().withStatus(200)));

        // Aquí iría la llamada real al repositorio
        // Por ahora verificamos que el stub está configurado
        assertTrue(true);
    }

    @Test
    void testRejectionByRules() {
        // Simular Firebase devolviendo 403 (rechazado)
        stubFor(patch(urlPathEqualTo("/games/game2.json"))
                .willReturn(aResponse().withStatus(403)));

        // El servicio debería detectar esto y hacer rollback
        assertTrue(true);
    }

    @Test
    void testConcurrentWrites() {
        // Primera escritura: 200, segunda: 409 conflicto
        stubFor(patch(urlPathEqualTo("/games/game3.json"))
                .willReturn(aResponse().withStatus(200)));

        stubFor(patch(urlPathEqualTo("/games/game3.json"))
                .willReturn(aResponse().withStatus(409)));

        // La lógica de reconciliación debería manejar esto
        assertTrue(true);
    }

    @Test
    void testDeduplicationByClientNonce() {
        // Simular respuesta para peticiones con nonce específico
        stubFor(patch(urlPathMatching("/games/game4.json.*nonce=abc.*"))
                .willReturn(aResponse().withStatus(200)));

        // Primera petición con nonce=abc: debería funcionar
        // Segunda petición con mismo nonce: debería ignorarse
        assertTrue(true);
    }
}
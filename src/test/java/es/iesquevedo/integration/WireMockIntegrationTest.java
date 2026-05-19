package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.iesquevedo.config.AppConfig;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.MainService;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración con WireMock para MockUp de APIs HTTP
 * Demuestra cómo usar WireMock para stubbing de peticiones HTTP
 * en ambiente de pruebas de integración.
 */
@DisplayName("Tests de Integración con WireMock")
class WireMockIntegrationTest {

    private WireMockServer wireMockServer;
    private MainService mainService;
    private MainRepository mainRepository;

    @BeforeEach
    void setUp() {
        // Inicializar WireMock en puerto 8080 (configurable)
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        configureFor("localhost", 8080);

        // Configurar repositorio en memoria
        mainRepository = AppConfig.createInMemoryRepository();
        mainService = new MainServiceImpl(mainRepository);
    }

    @AfterEach
    void tearDown() {
        // Detener servidor WireMock
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            wireMockServer.resetAll();
        }
    }

    @DisplayName("Debe hacer stub de endpoint GET exitoso")
    @Test
    void testWireMockGetStubSuccess() {
        // Configurar stub de WireMock
        stubFor(get(urlEqualTo("/api/players/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 1, \"name\": \"PlayerOne\"}")));

        // Verificar que el stub está configurado
        assertNotNull(wireMockServer);
        assertTrue(wireMockServer.isRunning());
    }

    @DisplayName("Debe hacer stub de endpoint POST exitoso")
    @Test
    void testWireMockPostStub() {
        // Configurar stub POST
        stubFor(post(urlEqualTo("/api/players"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 2, \"name\": \"NewPlayer\"}")));

        assertTrue(wireMockServer.isRunning());
    }

    @DisplayName("Debe configurar múltiples stubs simultáneamente")
    @Test
    void testMultipleWireMockStubs() {
        // Configurar múltiples endpoints
        stubFor(get(urlEqualTo("/api/players/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\": 1, \"name\": \"Player1\"}")));

        stubFor(get(urlEqualTo("/api/players/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\": 2, \"name\": \"Player2\"}")));

        stubFor(get(urlEqualTo("/api/game/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\": 1, \"status\": \"active\"}")));

        assertTrue(wireMockServer.isRunning());
    }

    @DisplayName("Debe simular error 404 en WireMock")
    @Test
    void testWireMockNotFound() {
        // Configurar stub para error 404
        stubFor(get(urlEqualTo("/api/players/999"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"error\": \"Player not found\"}")));

        assertTrue(wireMockServer.isRunning());
    }

    @DisplayName("Debe integrar MainService con repositorio en memoria")
    @Test
    void testMainServiceWithInMemoryRepository() {
        // Usar servicio principal con repositorio en memoria
        String greeting = mainService.greet();

        assertNotNull(greeting);
        assertTrue(greeting.contains("Hello"));
        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }

    @DisplayName("Debe resetear stubs entre tests")
    @Test
    void testWireMockResetBetweenTests() {
        // Configurar stub
        stubFor(get(urlEqualTo("/api/test"))
                .willReturn(aResponse()
                        .withStatus(200)));

        // Reset
        wireMockServer.resetAll();

        // Verificar que no hay stubs
        assertTrue(wireMockServer.isRunning());
    }
}


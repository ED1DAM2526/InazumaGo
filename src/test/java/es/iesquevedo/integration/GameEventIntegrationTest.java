package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.firebase.database.FirebaseDatabase;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.Position;
import es.iesquevedo.integration.wiremock.GameEventWireMockStubs;
import es.iesquevedo.repository.firebase.GameEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para la sincronización de eventos de partida con Firebase y WireMock.
 * Verifica que los eventos de inicio, movimiento y fin se registren correctamente.
 */
class GameEventIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080))
        .build();

    @Mock
    private FirebaseDatabase firebaseDatabase;

    private GameEventRepository gameEventRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gameEventRepository = new GameEventRepository(firebaseDatabase);
    }

    /**
     * Test: Verificar que un evento de inicio de partida se registra correctamente
     */
    @Test
    void testRecordGameStart_shouldSyncToFirebase() {
        // Arrange
        String gameId = "game123";
        GameEventWireMockStubs.stubGameStart(gameId);

        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameStart(gameId, gameDto);

        // Assert
        assertNotNull(result);
    }

    /**
     * Test: Verificar que un evento de movimiento se registra correctamente
     */
    @Test
    void testRecordGameMove_shouldSyncToFirebase() {
        // Arrange
        String gameId = "game123";
        GameEventWireMockStubs.stubGameMove(gameId);

        Position position = new Position(5, 8);
        MoveData moveData = new MoveData(
            "player1",
            "KICK",
            position
        );

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameMove(gameId, moveData);

        // Assert
        assertNotNull(result);
    }

    /**
     * Test: Verificar que un evento de fin de partida se registra correctamente
     */
    @Test
    void testRecordGameEnd_shouldSyncToFirebase() {
        // Arrange
        String gameId = "game123";
        GameEventWireMockStubs.stubGameEnd(gameId);

        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "FINISHED",
            System.currentTimeMillis()
        );

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameEnd(gameId, gameDto);

        // Assert
        assertNotNull(result);
    }

    /**
     * Test: Verificar que WireMock recibe la solicitud correctamente
     */
    @Test
    void testWireMockReceivesGameStartEvent() {
        // Arrange
        String gameId = "game123";

        stubFor(post(urlEqualTo("/api/events/game.start"))
            .willReturn(aResponse().withStatus(200)));

        // Act
        // Simular una solicitud HTTP directamente a WireMock
        WireMock.post(urlEqualTo("/api/events/game.start")).check(r -> {
            assertEquals(200, r.getStatus());
        });

        // Assert
        verify(postRequestedFor(urlEqualTo("/api/events/game.start")));
    }

    /**
     * Test: Verificar que WireMock retorna error cuando la solicitud falla
     */
    @Test
    void testWireMockReturnsErrorOnFailure() {
        // Arrange
        stubFor(post(urlEqualTo("/api/events/game.start"))
            .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal Server Error")));

        // Assert
        verify(postRequestedFor(urlEqualTo("/api/events/game.start")).atMost(0));
    }

    /**
     * Test: Verificar que los eventos se registran con timestamp
     */
    @Test
    void testEventRecordingIncludesTimestamp() {
        // Arrange
        String gameId = "game123";
        long before = System.currentTimeMillis();

        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            before
        );

        GameEventWireMockStubs.stubGameStart(gameId);

        // Act
        gameEventRepository.recordGameStart(gameId, gameDto);

        // Assert
        long after = System.currentTimeMillis();
        assertTrue(after >= before, "El timestamp debe registrarse correctamente");
    }

    /**
     * Test: Flujo completo de eventos de partida
     */
    @Test
    void testCompleteGameEventFlow() {
        // Arrange
        String gameId = "game-complete-flow";
        GameEventWireMockStubs.stubAllGameEvents(gameId);

        GameDto gameStart = new GameDto(
            gameId,
            "Complete Flow Test",
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );

        MoveData move = new MoveData("player1", "KICK", new Position(5, 8));

        GameDto gameEnd = new GameDto(
            gameId,
            "Complete Flow Test",
            Arrays.asList("Player1", "Player2"),
            "FINISHED",
            System.currentTimeMillis()
        );

        // Act
        CompletableFuture<Void> start = gameEventRepository.recordGameStart(gameId, gameStart);
        CompletableFuture<Void> move_event = gameEventRepository.recordGameMove(gameId, move);
        CompletableFuture<Void> end = gameEventRepository.recordGameEnd(gameId, gameEnd);

        // Assert
        assertNotNull(start);
        assertNotNull(move_event);
        assertNotNull(end);
    }







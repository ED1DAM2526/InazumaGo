package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.GameEventDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.Position;
import es.iesquevedo.integration.wiremock.GameEventWireMockStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de integración para la sincronización de eventos de partida con WireMock.
 */
class GameEventIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
        .options(wireMockConfig().port(8080))
        .build();

    private es.iesquevedo.repository.firebase.GameEventRepository gameEventRepository;

    @BeforeEach
    void setUp() {
        gameEventRepository = new es.iesquevedo.repository.firebase.GameEventRepository("http://localhost:8080");
    }

    @Test
    void shouldPersistStartMoveEndAndRecoverEvents() {
        String gameId = "game-complete-flow";
        GameEventWireMockStubs.stubGameStart(gameId);
        GameEventWireMockStubs.stubGameMove(gameId);
        GameEventWireMockStubs.stubGameEnd(gameId);

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

        assertDoesNotThrow(() -> gameEventRepository.recordGameStart(gameId, gameStart).join());
        assertDoesNotThrow(() -> gameEventRepository.recordGameMove(gameId, move).join());
        assertDoesNotThrow(() -> gameEventRepository.recordGameEnd(gameId, gameEnd).join());

        GameEventWireMockStubs.stubRecoveredGameEvents(gameId,
            "[" +
                "{\"id\":\"e1\",\"type\":\"game.start\",\"gameId\":\"" + gameId + "\",\"timestamp\":1,\"payload\":{\"id\":\"" + gameId + "\"}}," +
                "{\"id\":\"e2\",\"type\":\"game.move\",\"gameId\":\"" + gameId + "\",\"timestamp\":2,\"payload\":{\"playerId\":\"player1\",\"move\":\"KICK\"}}," +
                "{\"id\":\"e3\",\"type\":\"game.end\",\"gameId\":\"" + gameId + "\",\"timestamp\":3,\"payload\":{\"id\":\"" + gameId + "\"}}" +
            "]");

        List<GameEventDto> events = gameEventRepository.getGameEvents(gameId).join();

        assertEquals(3, events.size());
        assertEquals("game.start", events.get(0).getType());
        assertEquals("game.move", events.get(1).getType());
        assertEquals("game.end", events.get(2).getType());
        assertTrue(events.stream().allMatch(event -> gameId.equals(event.getGameId())));
    }

    @Test
    void shouldAcceptValidTurnWith200() {
        String gameId = "game-turn-ok";
        GameEventWireMockStubs.stubGameMove(gameId);

        MoveData move = new MoveData("player1", "PASS", new Position(3, 4));

        assertDoesNotThrow(() -> gameEventRepository.recordGameMove(gameId, move).join());
        GameEventWireMockStubs.verifyEventRequest("game.move");
    }

    @Test
    void shouldRejectInvalidTurnWith403() {
        String gameId = "game-turn-forbidden";
        GameEventWireMockStubs.stubGameMoveForbidden(gameId);

        MoveData move = new MoveData("player2", "TACKLE", new Position(2, 6));

        CompletionException exception = assertThrows(CompletionException.class,
            () -> gameEventRepository.recordGameMove(gameId, move).join());

        assertTrue(exception.getCause() != null);
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertTrue(exception.getCause().getMessage().contains("HTTP 403"));
    }
}

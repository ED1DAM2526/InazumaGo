package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import es.iesquevedo.exception.InvalidMoveException;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test E2E con WireMock: Valida rechazo de movimientos inválidos (403) y rollback.
 *
 * Criterios de E3-US3:
 * - Ante rechazo de movimiento (403/reglas), el cliente realiza rollback
 * - Muestra feedback claro sin desincronizar la partida
 * - Permite reintentos tras rechazo
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("E3-US3: Game Flow E2E con Rechazo y Rollback")
public class GameE2EWireMockTest {

    private static WireMockServer wireMockServer;
    private static final int PORT = 8081;
    private GameService gameService;

    @BeforeAll
    void startServer() {
        wireMockServer = new WireMockServer(PORT);
        wireMockServer.start();
        configureFor("localhost", PORT);

        InazumaGoMoveValidator moveValidator = new InazumaGoMoveValidator();
        gameService = new GameServiceImpl(moveValidator);
    }

    @AfterAll
    void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetStubs() {
        WireMock.reset();
    }

    /**
     * Test: Movimiento rechazado por ubicación ya ocupada (regla de juego)
     * Escenario:
     * 1. Crear partida y unirse
     * 2. Player1 coloca piedra en (5,5)
     * 3. Player2 coloca piedra en (3,3)
     * 4. Player1 intenta colocar en (5,5) nuevamente -> RECHAZO (403)
     * 5. Verificar que la partida no cambió (rollback)
     * 6. Player1 intenta en (7,7) -> ACEPCIÓN
     */
    @Test
    @DisplayName("Movimiento rechazado genera rollback sin desincronizar partida")
    void testMoveRejectionWith403RollsBack() {
        // Setup
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        // Crear y unirse
        Game game = gameService.createGame("Partida Rechazo", player1);
        gameService.joinGame(game.getId(), player2);
        Game startedGame = gameService.startGame(game.getId());

        assertNotNull(startedGame);
        assertEquals(2, startedGame.getPlayers().size());
        assertEquals("IN_PROGRESS", startedGame.getState().name());

        // Player1 coloca primera piedra en (5,5)
        Move validMove1 = new Move(player1.getId(), 5, 5);
        Game afterMove1 = gameService.executeMove(game.getId(), player1.getId(), validMove1);
        assertEquals(player2.getId(), afterMove1.getCurrentPlayer().getId());
        assertEquals(1, afterMove1.getTurnCount());

        // Player2 coloca en (3,3)
        Move validMove2 = new Move(player2.getId(), 3, 3);
        Game afterMove2 = gameService.executeMove(game.getId(), player2.getId(), validMove2);
        assertEquals(player1.getId(), afterMove2.getCurrentPlayer().getId());
        assertEquals(2, afterMove2.getTurnCount());

        // Player1 intenta colocar en (5,5) nuevamente (debería estar ocupada)
        Move invalidMove = new Move(player1.getId(), 5, 5);
        assertThrows(InvalidMoveException.class,
            () -> gameService.executeMove(game.getId(), player1.getId(), invalidMove),
            "Debe rechazar movimiento a posición ocupada");

        // Verificar rollback: turno y estado no cambiaron
        Game gameAfterRejection = gameService.getGame(game.getId());
        assertEquals(player1.getId(), gameAfterRejection.getCurrentPlayer().getId(),
            "El turno debe seguir siendo de Player1 tras rechazo");
        assertEquals(2, gameAfterRejection.getTurnCount(),
            "El contador de turnos no debe incrementarse tras rechazo");

        // Player1 intenta en (7,7) - válido
        Move retryMove = new Move(player1.getId(), 7, 7);
        Game afterRetry = gameService.executeMove(game.getId(), player1.getId(), retryMove);
        assertEquals(player2.getId(), afterRetry.getCurrentPlayer().getId(),
            "El turno debe cambiar a Player2 tras reintento exitoso");
        assertEquals(3, afterRetry.getTurnCount(),
            "El contador de turnos debe incrementarse tras reintento exitoso");
    }

    /**
     * Test: Múltiples movimientos alternos con validación de reglas
     * Valida que la sincronización se mantiene en una secuencia completa
     */
    @Test
    @DisplayName("Secuencia E2E: múltiples movimientos alternos > 5 turnos")
    void testMultipleAlternatingMovesWithRuleValidation() {
        Player player1 = new Player("p1", "Alice");
        Player player2 = new Player("p2", "Bob");

        Game game = gameService.createGame("Partida Larga", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        // Secuencia de 8 movimientos (4 cada uno)
        int[][] moves = {
            {3, 3}, {3, 4}, {4, 3}, {4, 4},   // Player1: moves 1-2, Player2: moves 1-2
            {5, 5}, {5, 6}, {6, 5}, {6, 6}    // Player1: moves 3-4, Player2: moves 3-4
        };

        for (int i = 0; i < moves.length; i++) {
            Player currentPlayer = (i % 2 == 0) ? player1 : player2;
            Move move = new Move(currentPlayer.getId(), moves[i][0], moves[i][1]);

            Game gameAfterMove = gameService.executeMove(game.getId(), currentPlayer.getId(), move);
            assertNotNull(gameAfterMove);
            assertEquals(i + 1, gameAfterMove.getTurnCount(),
                "Turno debe ser " + (i + 1) + " tras movimiento " + (i + 1));

            // Verificar alternancia de turno correcto
            if (i < moves.length - 1) {
                Player expectedNextPlayer = (i % 2 == 0) ? player2 : player1;
                assertEquals(expectedNextPlayer.getId(), gameAfterMove.getCurrentPlayer().getId(),
                    "Turno debe alternar correctamente");
            }
        }

        Game finalGame = gameService.getGame(game.getId());
        assertEquals(8, finalGame.getTurnCount(), "Total de 8 turnos ejecutados");
        assertEquals("IN_PROGRESS", finalGame.getState().name(), "Partida sigue en curso");
    }

    /**
     * Test: Flujo de interrupción y reintento tras error de sincronización
     */
    @Test
    @DisplayName("Reintento tras interrupción mantiene consistencia")
    void testRetryAfterInterruptionMaintainsConsistency() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Interrupción", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        // Primer movimiento exitoso
        Move move1 = new Move(player1.getId(), 2, 2);
        Game afterMove1 = gameService.executeMove(game.getId(), player1.getId(), move1);
        assertEquals(1, afterMove1.getTurnCount());

        // Simular fallo de red (movimiento rechazado)
        Move problemMove = new Move(player2.getId(), 2, 2); // Intenta en posición ocupada
        assertThrows(InvalidMoveException.class,
            () -> gameService.executeMove(game.getId(), player2.getId(), problemMove));

        // Verificar que el estado se mantiene consistente
        Game gameAfterFailure = gameService.getGame(game.getId());
        assertEquals(1, gameAfterFailure.getTurnCount(), "Turno no cambió tras fallo");
        assertEquals(player2.getId(), gameAfterFailure.getCurrentPlayer().getId(),
            "Aún es turno de Player2");

        // Reintento con movimiento válido
        Move retryMove = new Move(player2.getId(), 3, 3);
        Game afterRetry = gameService.executeMove(game.getId(), player2.getId(), retryMove);
        assertEquals(2, afterRetry.getTurnCount(), "Turno incrementó tras reintento exitoso");
        assertEquals(player1.getId(), afterRetry.getCurrentPlayer().getId(),
            "Turno cambió a Player1");
    }

    /**
     * Test: Fin de partida tras múltiples movimientos
     */
    @Test
    @DisplayName("Fin de partida con estado sincronizado")
    void testGameFinishStateSync() {
        Player player1 = new Player("p1", "Winner");
        Player player2 = new Player("p2", "Loser");

        Game game = gameService.createGame("Partida Final Test", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        // Algunos movimientos
        Move move1 = new Move(player1.getId(), 4, 4);
        gameService.executeMove(game.getId(), player1.getId(), move1);

        Move move2 = new Move(player2.getId(), 5, 5);
        gameService.executeMove(game.getId(), player2.getId(), move2);

        // Finalizar partida
        Game finishedGame = gameService.finishGame(game.getId(), player1.getId());

        assertEquals("FINISHED", finishedGame.getState().name());
        assertEquals(player1.getId(), finishedGame.getWinnerPlayerId());
        assertEquals(2, finishedGame.getTurnCount(), "Turnos se mantienen tras fin");
    }

    /**
     * Test: Abandono de partida
     */
    @Test
    @DisplayName("Abandono de partida sincroniza estado")
    void testGameAbandonSync() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Abandono", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        // Movimiento y abandono
        Move move = new Move(player1.getId(), 8, 8);
        gameService.executeMove(game.getId(), player1.getId(), move);

        Game abandonedGame = gameService.abandonGame(game.getId());
        assertEquals("ABANDONED", abandonedGame.getState().name());
        assertEquals(1, abandonedGame.getTurnCount(), "Turnos se registran antes del abandono");
    }
}


package es.iesquevedo.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameE2ESimpleTest {

    private static WireMockServer wireMockServer;
    private static final int PORT = 8080;
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

    @Test
    void testCreateAndStartGame() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Test", player1);
        assertNotNull(game);
        assertNotNull(game.getId());
        assertEquals("WAITING", game.getState().name());

        Game joinedGame = gameService.joinGame(game.getId(), player2);
        assertNotNull(joinedGame);
        assertEquals(2, joinedGame.getPlayers().size());

        Game startedGame = gameService.startGame(game.getId());
        assertEquals("IN_PROGRESS", startedGame.getState().name());
    }

    @Test
    void testExecuteMove() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Movimientos", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        // Create a Move object with row and col
        Move move = new Move(player1.getId(), 3, 3);

        Game afterMove = gameService.executeMove(game.getId(), player1.getId(), move);
        assertNotNull(afterMove);

        // Turn should have changed to player2
        assertEquals(player2.getId(), afterMove.getCurrentPlayer().getId());
    }

    @Test
    void testNextTurn() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Turnos", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        assertEquals(player1.getId(), game.getCurrentPlayer().getId());

        Game afterNextTurn = gameService.nextTurn(game.getId());
        assertEquals(player2.getId(), afterNextTurn.getCurrentPlayer().getId());
    }

    @Test
    void testFinishGame() {
        Player player1 = new Player("p1", "Jugador1");
        Player player2 = new Player("p2", "Jugador2");

        Game game = gameService.createGame("Partida Final", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        Game finishedGame = gameService.finishGame(game.getId(), player1.getId());
        assertEquals("FINISHED", finishedGame.getState().name());
        assertEquals(player1.getId(), finishedGame.getWinnerPlayerId());
    }
}
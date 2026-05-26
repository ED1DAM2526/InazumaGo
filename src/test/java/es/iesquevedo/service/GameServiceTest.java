package es.iesquevedo.service;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.GameServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de GameService.
 */
public class GameServiceTest {

    private GameService gameService;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        gameService = new GameServiceImpl();
        player1 = new Player("p1", "Alice");
        player2 = new Player("p2", "Bob");
    }

    @Test
    void testCreateGame() {
        Game game = gameService.createGame("Inazuma Match", player1);

        assertNotNull(game);
        assertEquals("Inazuma Match", game.getName());
        assertEquals(GameState.WAITING, game.getState());
        assertEquals(1, game.getPlayers().size());
        assertEquals(player1, game.getPlayers().get(0));
    }

    @Test
    void testJoinGame() {
        Game game = gameService.createGame("Inazuma Match", player1);
        game = gameService.joinGame(game.getId(), player2);

        assertEquals(2, game.getPlayers().size());
        assertEquals(player2, game.getPlayers().get(1));
        assertEquals(GameState.WAITING, game.getState());
    }

    @Test
    void testJoinGameCannotAddThirdPlayer() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);

        Player player3 = new Player("p3", "Charlie");
        assertThrows(IllegalStateException.class, () -> gameService.joinGame(game.getId(), player3));
    }

    @Test
    void testStartGame() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);

        game = gameService.startGame(game.getId());

        assertEquals(GameState.IN_PROGRESS, game.getState());
        assertEquals(0, game.getCurrentPlayerIndex());
    }

    @Test
    void testStartGameRequiresTwoPlayers() {
        Game game = gameService.createGame("Inazuma Match", player1);

        assertThrows(IllegalStateException.class, () -> gameService.startGame(game.getId()));
    }

    @Test
    void testNextTurn() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);
        game = gameService.startGame(game.getId());

        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(0, game.getTurnCount());

        game = gameService.nextTurn(game.getId());

        assertEquals(1, game.getCurrentPlayerIndex());
        assertEquals(1, game.getTurnCount());

        game = gameService.nextTurn(game.getId());

        assertEquals(0, game.getCurrentPlayerIndex());
        assertEquals(2, game.getTurnCount());
    }

    @Test
    void testExecuteMoveValidatesPlayerTurn() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);
        final Game startedGame = gameService.startGame(game.getId());

        // Intenta mover con player2 pero es turno de player1
        Move move = new Move(player2.getId(), 0, 0);
        assertThrows(PlayerNotInTurnException.class, 
            () -> gameService.executeMove(startedGame.getId(), player2.getId(), move));
    }

    @Test
    void testExecuteMoveValidatesPlayerAlive() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);
        final Game startedGame = gameService.startGame(game.getId());

        // Marca player1 como muerto
        startedGame.getCurrentPlayer().setAlive(false);

        Move move = new Move(player1.getId(), 0, 0);
        assertThrows(InvalidMoveException.class, 
            () -> gameService.executeMove(startedGame.getId(), player1.getId(), move));
    }

    @Test
    void testFinishGame() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        game = gameService.finishGame(game.getId(), player1.getId());

        assertEquals(GameState.FINISHED, game.getState());
        assertEquals(player1.getId(), game.getWinnerPlayerId());
        assertNotNull(game.getFinishedAt());
    }

    @Test
    void testAbandonGame() {
        Game game = gameService.createGame("Inazuma Match", player1);
        gameService.joinGame(game.getId(), player2);
        gameService.startGame(game.getId());

        game = gameService.abandonGame(game.getId());

        assertEquals(GameState.ABANDONED, game.getState());
        assertNotNull(game.getFinishedAt());
    }

    @Test
    void testGetGameReturnsNull() {
        assertNull(gameService.getGame("nonexistent"));
    }
}



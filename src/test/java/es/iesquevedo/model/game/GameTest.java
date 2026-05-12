package es.iesquevedo.model.game;

import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para Game (agregado raíz)
 */
public class GameTest {
    private Game game;
    private Player blackPlayer;
    private Player whitePlayer;
    
    @BeforeEach
    public void setUp() {
        game = new Game("game-123");
        blackPlayer = new Player("black1", PlayerColor.BLACK, "Negro");
        whitePlayer = new Player("white1", PlayerColor.WHITE, "Blanco");
    }
    
    @Test
    public void testGameCreation() {
        assertEquals("game-123", game.getGameId());
        assertEquals(GameState.WAITING, game.getState());
        assertEquals(PlayerColor.BLACK, game.getCurrentPlayer());
    }
    
    @Test
    public void testAddPlayers() {
        game.addPlayer(blackPlayer);
        game.addPlayer(whitePlayer);
        
        assertEquals(blackPlayer, game.getPlayer(PlayerColor.BLACK));
        assertEquals(whitePlayer, game.getPlayer(PlayerColor.WHITE));
    }
    
    @Test
    public void testCantAddMoreThanTwoPlayers() {
        game.addPlayer(blackPlayer);
        game.addPlayer(whitePlayer);
        
        Player extraPlayer = new Player("extra", PlayerColor.BLACK, "Extra");
        assertThrows(IllegalStateException.class, () -> game.addPlayer(extraPlayer));
    }
    
    @Test
    public void testStartGame() {
        game.addPlayer(blackPlayer);
        game.addPlayer(whitePlayer);
        game.startGame();
        
        assertEquals(GameState.PLAYING, game.getState());
        assertEquals(PlayerColor.BLACK, game.getCurrentPlayer());
    }
    
    @Test
    public void testNextTurn() {
        game.addPlayer(blackPlayer);
        game.addPlayer(whitePlayer);
        game.startGame();
        
        game.nextTurn();
        
        assertEquals(PlayerColor.WHITE, game.getCurrentPlayer());
        
        game.nextTurn();
        
        assertEquals(PlayerColor.BLACK, game.getCurrentPlayer());
    }
    
    @Test
    public void testRecordMove() {
        Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        game.recordMove(move);
        
        assertEquals(1, game.getMoveHistory().size());
        assertEquals(move, game.getMoveHistory().get(0));
    }
    
    @Test
    public void testPassCount() {
        game.handlePass();
        assertTrue(game.getMoveCount() == 0); // No hay movimientos registrados
        
        game.handlePass();
        
        assertEquals(GameState.FINISHED, game.getState());
    }
    
    @Test
    public void testResetPassCount() {
        game.handlePass();
        game.resetPassCount();
        
        game.handlePass();
        
        // Solo uno; no debe terminar
        assertEquals(GameState.WAITING, game.getState());
    }
}


package es.iesquevedo.integration;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.game.Game;
import es.iesquevedo.model.game.GameState;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.move.MoveExecutor;
import es.iesquevedo.model.move.MoveValidator;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import es.iesquevedo.model.scoring.ChineseScorerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración: simulación completa de una partida
 */
public class FullGameSimulationTest {
    private Game game;
    private Player blackPlayer;
    private Player whitePlayer;
    private MoveValidator validator;
    private MoveExecutor executor;
    private ChineseScorerImpl scorer;
    
    @BeforeEach
    public void setUp() {
        game = new Game("game-integration-test");
        blackPlayer = new Player("black1", PlayerColor.BLACK, "Negro");
        whitePlayer = new Player("white1", PlayerColor.WHITE, "Blanco");
        
        game.addPlayer(blackPlayer);
        game.addPlayer(whitePlayer);
        game.startGame();
        
        validator = new MoveValidator();
        executor = new MoveExecutor();
        scorer = new ChineseScorerImpl();
    }
    
    @Test
    public void testSimpleGameWithCapture() {
        assertTrue(game.isActive());
        
        // Move 1: Negro en 4,4
        Move move1 = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        assertTrue(validator.validate(move1, game.getBoard(), game.getCurrentPlayer(), 
                                      game.getBoardHistory()).isValid());
        executor.executeMove(move1, game.getBoard(), blackPlayer, whitePlayer);
        game.recordMove(move1);
        game.nextTurn();
        
        assertEquals(PlayerColor.WHITE, game.getCurrentPlayer());
        assertEquals(Stone.BLACK, game.getBoard().getStone(Position.of(4, 4)));
        
        // Move 2: Blanco en 4,5
        Move move2 = new Move(Position.of(4, 5), PlayerColor.WHITE, "nonce2");
        assertTrue(validator.validate(move2, game.getBoard(), game.getCurrentPlayer(), 
                                      game.getBoardHistory()).isValid());
        executor.executeMove(move2, game.getBoard(), whitePlayer, blackPlayer);
        game.recordMove(move2);
        game.nextTurn();
        
        assertEquals(PlayerColor.BLACK, game.getCurrentPlayer());
        
        // Continuar construyendo el tablero...
        // (simplificado para demo)
    }
    
    @Test
    public void testGameWithDoublePasse() {
        // Negro pasa
        Move pass1 = Move.pass(PlayerColor.BLACK, "nonce1");
        game.recordMove(pass1);
        game.handlePass();
        game.nextTurn();
        
        assertEquals(PlayerColor.WHITE, game.getCurrentPlayer());
        assertEquals(GameState.PLAYING, game.getState());
        
        // Blanco pasa
        Move pass2 = Move.pass(PlayerColor.WHITE, "nonce2");
        game.recordMove(pass2);
        game.handlePass();
        
        assertEquals(GameState.FINISHED, game.getState());
    }
    
    @Test
    public void testProvisionalScore() {
        // Colocar algunas piedras
        game.getBoard().placeStone(Position.of(0, 0), Stone.BLACK);
        game.getBoard().placeStone(Position.of(1, 1), Stone.BLACK);
        game.getBoard().placeStone(Position.of(8, 8), Stone.WHITE);
        
        var score = scorer.calculateProvisionalScore(
            game.getBoard(), blackPlayer, whitePlayer
        );
        
        assertNotNull(score);
        assertTrue(score.getBlackScore() > 0 || score.getBlackTerritory() > 0);
    }
    
    @Test
    public void testMultipleCaptureEvents() {
        // Escenario: Negro captura piedra blanca
        game.getBoard().placeStone(Position.of(4, 4), Stone.WHITE);
        game.getBoard().placeStone(Position.of(3, 4), Stone.BLACK);
        game.getBoard().placeStone(Position.of(5, 4), Stone.BLACK);
        game.getBoard().placeStone(Position.of(4, 3), Stone.BLACK);
        
        // Negro coloca en última libertad
        Move captureMvoe = new Move(Position.of(4, 5), PlayerColor.BLACK, "nonce-capture");
        var validation = validator.validate(captureMvoe, game.getBoard(), 
                                           PlayerColor.BLACK, game.getBoardHistory());
        
        assertTrue(validation.isValid());
        assertTrue(validation.getCapturedGroups().size() > 0);
        
        var result = executor.executeMove(captureMvoe, game.getBoard(), blackPlayer, whitePlayer);
        
        assertEquals(1, result.getCapturedStoneCount());
        assertEquals(1, whitePlayer.getCapturedStones());
    }
}


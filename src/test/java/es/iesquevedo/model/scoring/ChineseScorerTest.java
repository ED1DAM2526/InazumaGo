package es.iesquevedo.model.scoring;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.game.ScoreSnapshot;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para puntuación
 */
public class ChineseScorerTest {
    private ChineseScorerImpl scorer;
    private Board board;
    private Player blackPlayer;
    private Player whitePlayer;
    
    @BeforeEach
    public void setUp() {
        scorer = new ChineseScorerImpl();
        board = new Board();
        blackPlayer = new Player("black1", PlayerColor.BLACK, "Negro");
        whitePlayer = new Player("white1", PlayerColor.WHITE, "Blanco");
    }
    
    @Test
    public void testProvisionalScoreEmptyBoard() {
        ScoreSnapshot snapshot = scorer.calculateProvisionalScore(board, blackPlayer, whitePlayer);
        
        // Komi 5.5 para blanco, redondeado a 5
        assertTrue(snapshot.getWhiteScore() >= snapshot.getBlackScore());
    }
    
    @Test
    public void testProvisionalScoreWithPiedras() {
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        board.placeStone(Position.of(0, 1), Stone.BLACK);
        board.placeStone(Position.of(1, 0), Stone.WHITE);
        
        ScoreSnapshot snapshot = scorer.calculateProvisionalScore(board, blackPlayer, whitePlayer);
        
        assertEquals(2, snapshot.getBlackScore() - snapshot.getBlackTerritory());
        assertEquals(1, snapshot.getWhiteScore() - snapshot.getWhiteTerritory() - 5);
    }
    
    @Test
    public void testProvisionalScoreWithCaptures() {
        blackPlayer.addCaptures(3);
        whitePlayer.addCaptures(2);
        
        ScoreSnapshot snapshot = scorer.calculateProvisionalScore(board, blackPlayer, whitePlayer);
        
        assertEquals(3, snapshot.getBlackScore());
        assertTrue(snapshot.getWhiteScore() >= 2);
    }
}


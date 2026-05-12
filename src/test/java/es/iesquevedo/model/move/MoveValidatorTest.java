package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.PlayerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para validación de movimientos
 */
public class MoveValidatorTest {
    private Board board;
    private MoveValidator validator;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
        validator = new MoveValidator();
    }
    
    @Test
    public void testValidMoveOnEmptyBoard() {
        Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        var result = validator.validate(move, board, PlayerColor.BLACK, java.util.List.of());
        
        assertTrue(result.isValid());
    }
    
    @Test
    public void testInvalidMoveOutOfTurn() {
        Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        var result = validator.validate(move, board, PlayerColor.WHITE, java.util.List.of());
        
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("turno"));
    }
    
    @Test
    public void testInvalidMoveOccupiedPosition() {
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        Move move = new Move(Position.of(4, 4), PlayerColor.WHITE, "nonce1");
        
        var result = validator.validate(move, board, PlayerColor.WHITE, java.util.List.of());
        
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("ocupada"));
    }
    
    @Test
    public void testSuicideMoveDetected() {
        // Rodear una posición completamente
        board.placeStone(Position.of(3, 4), Stone.WHITE);
        board.placeStone(Position.of(5, 4), Stone.WHITE);
        board.placeStone(Position.of(4, 3), Stone.WHITE);
        board.placeStone(Position.of(4, 5), Stone.WHITE);
        
        Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        var result = validator.validate(move, board, PlayerColor.BLACK, java.util.List.of());
        
        assertFalse(result.isValid());
        assertTrue(result.getReason().contains("suicidio"));
    }
    
    @Test
    public void testValidMoveCapturesEnemy() {
        // Dejar piedra negra sin libertades
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        board.placeStone(Position.of(3, 4), Stone.WHITE);
        board.placeStone(Position.of(5, 4), Stone.WHITE);
        board.placeStone(Position.of(4, 3), Stone.WHITE);
        // Una libertad en 4,5
        
        // Blanco cierra la última libertad (esto es captura)
        Move move = new Move(Position.of(4, 5), PlayerColor.WHITE, "nonce1");
        var result = validator.validate(move, board, PlayerColor.WHITE, java.util.List.of());
        
        assertTrue(result.isValid());
        assertTrue(result.getCapturedGroups().size() > 0);
    }
    
    @Test
    public void testPassIsAlwaysValid() {
        Move pass = Move.pass(PlayerColor.BLACK, "nonce1");
        var result = validator.validate(pass, board, PlayerColor.BLACK, java.util.List.of());
        
        assertTrue(result.isValid());
    }
}


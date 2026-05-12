package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ejecución de movimientos
 */
public class MoveExecutorTest {
    private Board board;
    private MoveExecutor executor;
    private Player blackPlayer;
    private Player whitePlayer;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
        executor = new MoveExecutor();
        blackPlayer = new Player("black1", PlayerColor.BLACK, "Negro");
        whitePlayer = new Player("white1", PlayerColor.WHITE, "Blanco");
    }
    
    @Test
    public void testExecuteSimpleMove() {
        Move move = new Move(Position.of(4, 4), PlayerColor.BLACK, "nonce1");
        MoveResult result = executor.executeMove(move, board, blackPlayer, whitePlayer);
        
        assertTrue(result.isValid());
        assertEquals(Stone.BLACK, board.getStone(Position.of(4, 4)));
    }
    
    @Test
    public void testExecuteMoveWithCapture() {
        // Colocar piedra blanca sin libertad (excepto una)
        board.placeStone(Position.of(4, 4), Stone.WHITE);
        board.placeStone(Position.of(3, 4), Stone.BLACK);
        board.placeStone(Position.of(5, 4), Stone.BLACK);
        board.placeStone(Position.of(4, 3), Stone.BLACK);
        
        // Negro coloca en la última libertad (captura blanca)
        Move move = new Move(Position.of(4, 5), PlayerColor.BLACK, "nonce1");
        MoveResult result = executor.executeMove(move, board, blackPlayer, whitePlayer);
        
        assertTrue(result.isValid());
        assertTrue(result.hasCaptured());
        assertEquals(1, result.getCapturedStoneCount());
        assertEquals(Stone.EMPTY, board.getStone(Position.of(4, 4)));
        
        // Verificar que se incrementó contador de prisioneros
        assertEquals(1, whitePlayer.getCapturedStones());
    }
    
    @Test
    public void testExecutePass() {
        Move pass = Move.pass(PlayerColor.BLACK, "nonce1");
        MoveResult result = executor.executeMove(pass, board, blackPlayer, whitePlayer);
        
        assertTrue(result.isValid());
        assertTrue(board.isEmpty());
        assertEquals(0, result.getCapturedStoneCount());
    }
}


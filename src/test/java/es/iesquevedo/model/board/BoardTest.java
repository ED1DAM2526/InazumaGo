package es.iesquevedo.model.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para Board
 */
public class BoardTest {
    private Board board;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
    }
    
    @Test
    public void testBoardInitialization() {
        assertTrue(board.isEmpty());
        assertEquals(0, board.countStones(Stone.BLACK));
        assertEquals(0, board.countStones(Stone.WHITE));
    }
    
    @Test
    public void testPlaceStone() {
        Position pos = Position.of(0, 0);
        board.placeStone(pos, Stone.BLACK);
        assertEquals(Stone.BLACK, board.getStone(pos));
        assertFalse(board.isEmpty());
    }
    
    @Test
    public void testCantPlaceTwiceInSamePosition() {
        Position pos = Position.of(0, 0);
        board.placeStone(pos, Stone.BLACK);
        assertThrows(IllegalArgumentException.class, 
            () -> board.placeStone(pos, Stone.WHITE));
    }
    
    @Test
    public void testRemoveStone() {
        Position pos = Position.of(0, 0);
        board.placeStone(pos, Stone.BLACK);
        board.removeStone(pos);
        assertEquals(Stone.EMPTY, board.getStone(pos));
    }
    
    @Test
    public void testCountStones() {
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        board.placeStone(Position.of(1, 1), Stone.BLACK);
        board.placeStone(Position.of(2, 2), Stone.WHITE);
        
        assertEquals(2, board.countStones(Stone.BLACK));
        assertEquals(1, board.countStones(Stone.WHITE));
    }
    
    @Test
    public void testBoardCopy() {
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        Board copy = board.copy();
        
        assertEquals(Stone.BLACK, copy.getStone(Position.of(0, 0)));
        
        // Modificar copia no afecta original
        copy.removeStone(Position.of(0, 0));
        assertEquals(Stone.BLACK, board.getStone(Position.of(0, 0)));
    }
    
    @Test
    public void testBoardEquality() {
        Board board1 = new Board();
        Board board2 = new Board();
        
        board1.placeStone(Position.of(0, 0), Stone.BLACK);
        board2.placeStone(Position.of(0, 0), Stone.BLACK);
        
        assertEquals(board1, board2);
    }
}


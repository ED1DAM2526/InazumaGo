package es.iesquevedo.model.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para Position
 */
public class PositionTest {
    
    @Test
    public void testPositionCreation() {
        Position pos = Position.of(0, 0);
        assertEquals(0, pos.getRow());
        assertEquals(0, pos.getCol());
    }
    
    @Test
    public void testPositionBounds() {
        assertThrows(IllegalArgumentException.class, () -> Position.of(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> Position.of(9, 0));
        assertThrows(IllegalArgumentException.class, () -> Position.of(0, 9));
    }
    
    @Test
    public void testOrthogonalNeighbors() {
        Position center = Position.of(4, 4);
        var neighbors = center.getOrthogonalNeighbors();
        assertEquals(4, neighbors.size());
    }
    
    @Test
    public void testCornerNeighbors() {
        Position corner = Position.of(0, 0);
        var neighbors = corner.getOrthogonalNeighbors();
        assertEquals(2, neighbors.size());
    }
    
    @Test
    public void testPositionCaching() {
        Position pos1 = Position.of(0, 0);
        Position pos2 = Position.of(0, 0);
        assertSame(pos1, pos2);
    }
    
    @Test
    public void testToGoNotation() {
        Position pos = Position.of(0, 0);
        assertEquals("a9", pos.toGoNotation());
    }
}


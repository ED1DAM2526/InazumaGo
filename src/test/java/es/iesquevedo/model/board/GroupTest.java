package es.iesquevedo.model.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para Group
 */
public class GroupTest {
    private Board board;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
    }
    
    @Test
    public void testGroupCreation() {
        Group group = new Group(Stone.BLACK);
        assertEquals(Stone.BLACK, group.getColor());
        assertEquals(0, group.getSize());
    }
    
    @Test
    public void testAddStoneToGroup() {
        Group group = new Group(Stone.BLACK);
        Position pos = Position.of(0, 0);
        group.addStone(pos);
        
        assertEquals(1, group.getSize());
        assertTrue(group.getStones().contains(pos));
    }
    
    @Test
    public void testCountLiberties() {
        // Crear grupo de una piedra en el centro
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        Group group = new Group(Stone.BLACK);
        group.addStone(Position.of(4, 4));
        
        int liberties = group.countLiberties(board);
        assertEquals(4, liberties); // 4 vecinos ortogonales
    }
    
    @Test
    public void testLiertiesReducedByOtherPiedras() {
        // Grupo de piedra negra rodeada por blancas
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        board.placeStone(Position.of(3, 4), Stone.WHITE);
        board.placeStone(Position.of(5, 4), Stone.WHITE);
        
        Group group = new Group(Stone.BLACK);
        group.addStone(Position.of(4, 4));
        
        int liberties = group.countLiberties(board);
        assertEquals(2, liberties);
    }
    
    @Test
    public void testGroupIsAlive() {
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        Group group = new Group(Stone.BLACK);
        group.addStone(Position.of(4, 4));
        
        assertTrue(group.isAlive(board));
    }
    
    @Test
    public void testGroupIsDead() {
        // Rodear completamente una piedra
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        board.placeStone(Position.of(3, 4), Stone.WHITE);
        board.placeStone(Position.of(5, 4), Stone.WHITE);
        board.placeStone(Position.of(4, 3), Stone.WHITE);
        board.placeStone(Position.of(4, 5), Stone.WHITE);
        
        Group group = new Group(Stone.BLACK);
        group.addStone(Position.of(4, 4));
        
        assertFalse(group.isAlive(board));
    }
}


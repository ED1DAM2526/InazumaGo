package es.iesquevedo.model.board;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para BoardAnalyzer
 */
public class BoardAnalyzerTest {
    private Board board;
    private BoardAnalyzer analyzer;
    
    @BeforeEach
    public void setUp() {
        board = new Board();
        analyzer = new BoardAnalyzer();
    }
    
    @Test
    public void testFindSingleGroup() {
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        List<Group> groups = analyzer.findAllGroups(board);
        
        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).getSize());
    }
    
    @Test
    public void testFindConnectedGroup() {
        // Crear grupo conectado
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        board.placeStone(Position.of(4, 5), Stone.BLACK);
        board.placeStone(Position.of(5, 4), Stone.BLACK);
        
        List<Group> groups = analyzer.findAllGroups(board);
        
        assertEquals(1, groups.size());
        assertEquals(3, groups.get(0).getSize());
    }
    
    @Test
    public void testFindMultipleGroups() {
        // Grupo negro 1
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        board.placeStone(Position.of(0, 1), Stone.BLACK);
        
        // Grupo negro 2 (separado)
        board.placeStone(Position.of(3, 3), Stone.BLACK);
        
        // Grupo blanco
        board.placeStone(Position.of(5, 5), Stone.WHITE);
        
        List<Group> groups = analyzer.findAllGroups(board);
        
        assertEquals(3, groups.size());
    }
    
    @Test
    public void testFindGroupAt() {
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        Group group = analyzer.findGroupAt(board, Position.of(4, 4));
        
        assertNotNull(group);
        assertEquals(1, group.getSize());
    }
    
    @Test
    public void testFindCapturedGroups() {
        // Crear piedra negra sin libertades
        board.placeStone(Position.of(4, 4), Stone.BLACK);
        board.placeStone(Position.of(3, 4), Stone.WHITE);
        board.placeStone(Position.of(5, 4), Stone.WHITE);
        board.placeStone(Position.of(4, 3), Stone.WHITE);
        board.placeStone(Position.of(4, 5), Stone.WHITE);
        
        List<Group> captured = analyzer.findCapturedGroups(board);
        
        assertEquals(1, captured.size());
    }
    
    @Test
    public void testFindTerritories() {
        // Crear un territorio
        board.placeStone(Position.of(0, 0), Stone.BLACK);
        board.placeStone(Position.of(0, 2), Stone.BLACK);
        board.placeStone(Position.of(2, 0), Stone.BLACK);
        board.placeStone(Position.of(2, 2), Stone.BLACK);
        
        List<BoardAnalyzer.Territory> territories = analyzer.findTerritories(board);
        
        assertTrue(territories.size() > 0);
        // Al menos un territorio debe ser exclusivo del negro o neutro
    }
}


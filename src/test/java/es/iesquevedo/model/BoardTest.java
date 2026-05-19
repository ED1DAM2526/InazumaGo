package es.iesquevedo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testInitialBoardIsEmpty() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                assertEquals(0, board.getCell(r, c), "Tablero debe estar vacío inicialmente");
            }
        }
    }

    @Test
    void testPlaceAndRemoveStone() {
        board.placeStone(0, 0, 1); // Colocar piedra negra
        assertEquals(1, board.getCell(0, 0));

        board.removeStone(0, 0);
        assertEquals(0, board.getCell(0, 0));
    }

    @Test
    void testIsValid() {
        assertTrue(board.isValid(0, 0));
        assertTrue(board.isValid(8, 8));
        assertFalse(board.isValid(-1, 0));
        assertFalse(board.isValid(9, 0));
        assertFalse(board.isValid(0, -1));
        assertFalse(board.isValid(0, 9));
    }

    @Test
    void testIsEmpty() {
        assertTrue(board.isEmpty(0, 0));

        board.placeStone(0, 0, 1);
        assertFalse(board.isEmpty(0, 0));
    }

    @Test
    void testGetNeighbors() {
        // Centro: debe tener 4 vecinos
        var neighbors = board.getNeighbors(4, 4);
        assertEquals(4, neighbors.size());

        // Esquina: debe tener 2 vecinos
        neighbors = board.getNeighbors(0, 0);
        assertEquals(2, neighbors.size());
    }

    @Test
    void testCountLibertiesForSingleStone() {
        board.placeStone(4, 4, 1);
        int liberties = board.countLibertiesForGroup(4, 4);
        assertEquals(4, liberties, "Piedra aislada debe tener 4 libertades");
    }

    @Test
    void testCountLibertiesForConnectedGroup() {
        // Grupo de 2 piedras conectadas
        board.placeStone(4, 4, 1);
        board.placeStone(4, 5, 1);

        int liberties = board.countLibertiesForGroup(4, 4);
        assertEquals(6, liberties, "Grupo de 2 debe tener 6 libertades (sin contar duplicadas)");
    }

    @Test
    void testCountLibertiesWithBlockedGroup() {
        // Piedra negra rodeada parcialmente
        board.placeStone(4, 4, 1);
        board.placeStone(3, 4, 2); // Blanco arriba
        board.placeStone(5, 4, 2); // Blanco abajo

        int liberties = board.countLibertiesForGroup(4, 4);
        assertEquals(2, liberties, "Piedra con 2 lados bloqueados debe tener 2 libertades");
    }

    @Test
    void testGetGroup() {
        // Crear grupo de 3 piedras conectadas (L-shape)
        board.placeStone(4, 4, 1);
        board.placeStone(4, 5, 1);
        board.placeStone(5, 5, 1);

        Set<String> group = board.getGroup(4, 4);
        assertEquals(3, group.size(), "Grupo debe tener 3 piedras");
        assertTrue(group.contains("4,4"));
        assertTrue(group.contains("4,5"));
        assertTrue(group.contains("5,5"));
    }

    @Test
    void testCaptureGroupWithoutLiberties() {
        // Rodear completamente un grupo blanco
        // . . W . .
        // . N N N .
        // . W W W .
        board.placeStone(0, 2, 2); // Blanco arriba
        board.placeStone(1, 1, 1); // Negro izq
        board.placeStone(1, 2, 1); // Negro centro
        board.placeStone(1, 3, 1); // Negro der
        board.placeStone(2, 1, 2); // Blanco izq
        board.placeStone(2, 2, 2); // Blanco centro
        board.placeStone(2, 3, 2); // Blanco der

        // Cerrar: Negro coloca en (0,3) capturando Blanco
        board.placeStone(0, 3, 1);
        board.placeStone(1, 0, 1); // Pared izquierda
        board.placeStone(0, 1, 1); // Pared arriba-izq

        int captured = board.captureGroupsWithoutLiberties();
        assertTrue(captured > 0, "Debe haber capturado piedras");

        // Verificar que las piedras blancas fueron removidas
        assertEquals(0, board.getCell(0, 2), "Piedra blanca (0,2) debe estar capturada");
    }

    @Test
    void testBoardClone() {
        board.placeStone(0, 0, 1);
        board.placeStone(1, 1, 2);

        Board cloned = board.clone();
        assertEquals(board.getCell(0, 0), cloned.getCell(0, 0));
        assertEquals(board.getCell(1, 1), cloned.getCell(1, 1));

        // Modificar el clon no debe afectar el original
        cloned.placeStone(2, 2, 1);
        assertEquals(0, board.getCell(2, 2), "Original no debe ser afectado");
        assertEquals(1, cloned.getCell(2, 2), "Clon debe tener el cambio");
    }

    @Test
    void testBoardEquals() {
        Board board2 = new Board();
        assertTrue(board.equals(board2), "Dos tableros vacíos deben ser iguales");

        board.placeStone(0, 0, 1);
        assertFalse(board.equals(board2), "Tableros con diferentes estados deben ser distintos");

        board2.placeStone(0, 0, 1);
        assertTrue(board.equals(board2), "Tableros con mismo estado deben ser iguales");
    }
}

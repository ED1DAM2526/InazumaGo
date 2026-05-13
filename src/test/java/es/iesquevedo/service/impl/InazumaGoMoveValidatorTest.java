package es.iesquevedo.service.impl;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InazumaGoMoveValidatorTest {

    private InazumaGoMoveValidator validator;
    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        validator = new InazumaGoMoveValidator();
        player1 = new Player("p1", "Negro");
        player2 = new Player("p2", "Blanco");
        game = new Game("test", player1);
        game.addPlayer(player2);
        game.start();
    }

    @Test
    void testValidPlyacementMove() {
        Move move = new Move("p1", 0, 0);
        assertDoesNotThrow(() -> validator.validateMove(game, move));
    }

    @Test
    void testValidPassMove() {
        Move move = new Move("p1", true);
        assertDoesNotThrow(() -> validator.validateMove(game, move));
    }

    @Test
    void testNotPlayerTurnThrows() {
        Move move = new Move("p2", 0, 0); // Blanco intenta jugar pero es turno de Negro
        assertThrows(PlayerNotInTurnException.class, () -> validator.validateMove(game, move));
    }

    @Test
    void testGameNotInProgressThrows() {
        game.setState(GameState.FINISHED);
        Move move = new Move("p1", 0, 0);
        assertThrows(InvalidMoveException.class, () -> validator.validateMove(game, move));
    }

    @Test
    void testPositionOutOfBoundsThrows() {
        Move move = new Move("p1", 9, 0); // Fuera del tablero 9x9
        assertThrows(InvalidMoveException.class, () -> validator.validateMove(game, move));

        Move move2 = new Move("p1", -1, 5);
        assertThrows(InvalidMoveException.class, () -> validator.validateMove(game, move2));
    }

    @Test
    void testOccupiedPositionThrows() {
        Board board = game.getBoard();
        board.placeStone(0, 0, 1); // Colocar piedra negra

        Move move = new Move("p1", 0, 0);
        assertThrows(InvalidMoveException.class, () -> validator.validateMove(game, move));
    }

    @Test
    void testSuicideMoveThrows() {
        // Crear una situación de suicidio: piedra rodeada sin libertades
        Board board = game.getBoard();
        // Rodear posición (2,2) de manera que no haya libertades
        board.placeStone(1, 2, 2); // Blanco arriba
        board.placeStone(3, 2, 2); // Blanco abajo
        board.placeStone(2, 1, 2); // Blanco izquierda
        board.placeStone(2, 3, 2); // Blanco derecha

        Move move = new Move("p1", 2, 2); // Negro intenta colocar en centro rodeado
        assertThrows(InvalidMoveException.class, () -> validator.validateMove(game, move));
    }

    @Test
    void testCaptureMovesAllowed() {
        // Crear situación donde Negro captura: piedra blanca rodeada
        Board board = game.getBoard();
        board.placeStone(0, 1, 2); // Blanco en (0,1)
        board.placeStone(1, 0, 1); // Negro en (1,0)
        board.placeStone(1, 2, 1); // Negro en (1,2)
        board.placeStone(2, 1, 1); // Negro en (2,1)

        // Negro coloca en (0,0) capturando a Blanco
        Move move = new Move("p1", 0, 0);
        assertDoesNotThrow(() -> validator.validateMove(game, move));
    }

    @Test
    void testSecondPlayerTurnAfterFirst() {
        game.nextTurn(); // Cambiar a turno de Blanco

        Move move = new Move("p2", 1, 1);
        assertDoesNotThrow(() -> validator.validateMove(game, move));
    }
}


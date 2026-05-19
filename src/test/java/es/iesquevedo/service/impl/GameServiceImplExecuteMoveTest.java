package es.iesquevedo.service.impl;

import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameServiceImplExecuteMoveTest {

    private GameServiceImpl gameService;
    private Game game;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        gameService = new GameServiceImpl();
        player1 = new Player("p1", "Negro");
        player2 = new Player("p2", "Blanco");

        game = gameService.createGame("testGame", player1);
        gameService.joinGame(game.getId(), player2);
        game = gameService.startGame(game.getId());
    }

    @Test
    void testPlaceStoneChangesBoard() {
        Move move = new Move("p1", 0, 0);
        Game result = gameService.executeMove(game.getId(), "p1", move);

        assertEquals(1, result.getBoard().getCell(0, 0)); // Negro (1) en (0,0)
        assertEquals(1, result.getMoves().size());
        assertEquals(1, result.getCurrentPlayerIndex()); // Cambió a Blanco
    }

    @Test
    void testDoublPassEndsGame() {
        // Negro pasa
        Move pass1 = new Move("p1", true);
        game = gameService.executeMove(game.getId(), "p1", pass1);
        assertEquals(1, game.getConsecutivePasses());
        assertEquals(GameState.IN_PROGRESS, game.getState());

        // Blanco pasa
        Move pass2 = new Move("p2", true);
        game = gameService.executeMove(game.getId(), "p2", pass2);
        assertEquals(GameState.FINISHED, game.getState());
        assertNotNull(game.getWinnerPlayerId());
    }

    @Test
    void testCaptureRemovesPieces() {
        // Crear una situación de captura correcta:
        // Rodear una piedra blanca solitaria en (3,3) con piedras negras
        // N en (2,3)
        Move m1 = new Move("p1", 2, 3);
        game = gameService.executeMove(game.getId(), "p1", m1);
        
        // B en (3,3) - la piedra que será capturada
        Move m2 = new Move("p2", 3, 3);
        game = gameService.executeMove(game.getId(), "p2", m2);
        
        // N en (4,3)
        Move m3 = new Move("p1", 4, 3);
        game = gameService.executeMove(game.getId(), "p1", m3);
        
        // B pasa
        Move m4 = new Move("p2", true);
        game = gameService.executeMove(game.getId(), "p2", m4);
        
        // N en (3,2)
        Move m5 = new Move("p1", 3, 2);
        game = gameService.executeMove(game.getId(), "p1", m5);
        
        // B pasa
        Move m6 = new Move("p2", true);
        game = gameService.executeMove(game.getId(), "p2", m6);
        
        // N en (3,4) y captura la piedra blanca en (3,3)
        Move m7 = new Move("p1", 3, 4);
        game = gameService.executeMove(game.getId(), "p1", m7);
        
        // Verificar que la piedra blanca fue capturada
        assertEquals(0, game.getBoard().getCell(3, 3), 
            "La piedra blanca en (3,3) debe estar capturada (rodeada por Negro)");
    }

    @Test
    void testConsecutivePassesResetAfterMove() {
        // Negro pasa
        Move pass1 = new Move("p1", true);
        game = gameService.executeMove(game.getId(), "p1", pass1);
        assertEquals(1, game.getConsecutivePasses());

        // Blanco juega (no pasa)
        Move move = new Move("p2", 0, 0);
        game = gameService.executeMove(game.getId(), "p2", move);
        assertEquals(0, game.getConsecutivePasses(), "Los pases consecutivos deben resetear con un movimiento");
    }

    @Test
    void testMoveRecordedInHistory() {
        Move move1 = new Move("p1", 0, 0);
        game = gameService.executeMove(game.getId(), "p1", move1);
        assertEquals(1, game.getMoves().size());

        Move move2 = new Move("p2", 1, 1);
        game = gameService.executeMove(game.getId(), "p2", move2);
        assertEquals(2, game.getMoves().size());
    }

    @Test
    void testTurnAlternatesCorrectly() {
        assertEquals(0, game.getCurrentPlayerIndex(), "Debe empezar con Negro (índice 0)");

        Move move1 = new Move("p1", 0, 0);
        game = gameService.executeMove(game.getId(), "p1", move1);
        assertEquals(1, game.getCurrentPlayerIndex(), "Debe cambiar a Blanco (índice 1)");

        Move move2 = new Move("p2", 1, 1);
        game = gameService.executeMove(game.getId(), "p2", move2);
        assertEquals(0, game.getCurrentPlayerIndex(), "Debe cambiar a Negro (índice 0)");
    }

    @Test
    void testPassDoesNotPlaceStone() {
        Move pass = new Move("p1", true);
        game = gameService.executeMove(game.getId(), "p1", pass);

        // El tablero debe seguir vacío
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                assertEquals(0, game.getBoard().getCell(r, c), "Tablero debe estar vacío después de pase");
            }
        }
    }
}



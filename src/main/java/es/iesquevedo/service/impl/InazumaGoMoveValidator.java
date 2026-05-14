package es.iesquevedo.service.impl;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.MoveValidator;

/**
 * Validador de movimientos para Inazuma Go.
 * Implementa las reglas del juego: turno, posición, libertades, suicidio, etc.
 */
public class InazumaGoMoveValidator implements MoveValidator {

    @Override
    public void validateMove(Game game, Move move) throws InvalidMoveException, PlayerNotInTurnException {
        // Verificar que la partida está en progreso
        if (game.getState() != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("La partida no está en progreso");
        }

        // Verificar que es el turno del jugador
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.getId().equals(move.getPlayerId())) {
            throw new PlayerNotInTurnException("No es el turno del jugador: " + move.getPlayerId());
        }

        // Verificar que el jugador está vivo
        if (!currentPlayer.isAlive()) {
            throw new InvalidMoveException("El jugador no está vivo");
        }

        // Si es un pase, es válido
        if (move.isPass()) {
            return;
        }

        // Validar posición
        if (move.getRow() < 0 || move.getRow() >= 9 || move.getCol() < 0 || move.getCol() >= 9) {
            throw new InvalidMoveException("Posición fuera del tablero: (" + move.getRow() + "," + move.getCol() + ")");
        }

        // La posición debe estar vacía
        Board board = game.getBoard();
        if (!board.isEmpty(move.getRow(), move.getCol())) {
            throw new InvalidMoveException("La posición ya está ocupada: (" + move.getRow() + "," + move.getCol() + ")");
        }

        // Validar que el movimiento no es suicidio
        validateNotSuicide(board, move, game);
    }

    /**
     * Valida que el movimiento no deja el grupo sin libertades (suicidio).
     * Un movimiento es suicidio si:
     * - Coloca una piedra que quedaría sin libertades
     * - Y no captura piedras enemigas que restauren libertades
     */
    private void validateNotSuicide(Board board, Move move, Game game) throws InvalidMoveException {
        Board testBoard = board.clone();

        // Colocar la piedra (color: 1 para jugador 0, 2 para jugador 1)
        int color = game.getCurrentPlayerIndex() == 0 ? 1 : 2;
        testBoard.placeStone(move.getRow(), move.getCol(), color);

        // Capturar grupos enemigos sin libertades
        testBoard.captureGroupsWithoutLiberties();

        // Si el grupo del jugador sigue sin libertades, es suicidio
        if (testBoard.countLibertiesForGroup(move.getRow(), move.getCol()) == 0) {
            throw new InvalidMoveException("Movimiento es suicidio: la piedra quedaría sin libertades");
        }
    }
}



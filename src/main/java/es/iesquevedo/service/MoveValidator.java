package es.iesquevedo.service;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Move;

/**
 * Interfaz para validar movimientos en una partida de Inazuma Go.
 */
public interface MoveValidator {
    /**
     * Valida un movimiento en el contexto de una partida.
     * 
     * @param game La partida actual
     * @param move El movimiento a validar
     * @throws InvalidMoveException si el movimiento es inválido
     * @throws PlayerNotInTurnException si no es el turno del jugador
     */
    void validateMove(Game game, Move move) throws InvalidMoveException, PlayerNotInTurnException;
}

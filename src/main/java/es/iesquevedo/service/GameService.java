package es.iesquevedo.service;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Player;

/**
 * Servicio de lógica de juego.
 * Gestiona turnos, validación de movimientos y transiciones de estado.
 */
public interface GameService {

    /**
     * Crea una nueva partida.
     *
     * @param gameName nombre de la partida.
     * @param player1  primer jugador (creador).
     * @return partida nueva creada.
     */
    Game createGame(String gameName, Player player1);

    /**
     * Un jugador se une a una partida existente.
     *
     * @param gameId  ID de la partida.
     * @param player2 jugador que se une.
     * @return partida actualizada con el segundo jugador.
     */
    Game joinGame(String gameId, Player player2);

    /**
     * Inicia la partida (WAITING -> IN_PROGRESS).
     *
     * @param gameId ID de la partida.
     * @return partida iniciada.
     */
    Game startGame(String gameId);

    /**
     * Valida y ejecuta un movimiento de un jugador.
     * Lanza excepción si no es turno del jugador o el movimiento es inválido.
     *
     * @param gameId   ID de la partida.
     * @param playerId ID del jugador.
     * @param moveData datos del movimiento (dirección, acción, etc.).
     * @return partida actualizada después del movimiento.
     * @throws PlayerNotInTurnException si no es turno del jugador.
     * @throws InvalidMoveException     si el movimiento viola reglas.
     */
    Game executeMove(String gameId, String playerId, Object moveData);

    /**
     * Pasa el turno al siguiente jugador.
     *
     * @param gameId ID de la partida.
     * @return partida con turno avanzado.
     */
    Game nextTurn(String gameId);

    /**
     * Finaliza la partida con un ganador.
     *
     * @param gameId        ID de la partida.
     * @param winnerPlayerId ID del jugador ganador.
     * @return partida finalizada.
     */
    Game finishGame(String gameId, String winnerPlayerId);

    /**
     * Abandona la partida.
     *
     * @param gameId ID de la partida.
     * @return partida abandonada.
     */
    Game abandonGame(String gameId);

    /**
     * Obtiene una partida por su ID.
     *
     * @param gameId ID de la partida.
     * @return partida o null si no existe.
     */
    Game getGame(String gameId);
}

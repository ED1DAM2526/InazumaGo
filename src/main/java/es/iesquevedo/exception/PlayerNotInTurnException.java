package es.iesquevedo.exception;

/**
 * Excepción lanzada cuando un jugador intenta mover fuera de su turno.
 */
public class PlayerNotInTurnException extends RuntimeException {
    public PlayerNotInTurnException(String message) {
        super(message);
    }

    public PlayerNotInTurnException(String message, Throwable cause) {
        super(message, cause);
    }
}

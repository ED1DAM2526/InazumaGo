package es.iesquevedo.exception;

/**
 * Excepción lanzada cuando un movimiento viola las reglas del juego.
 */
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}

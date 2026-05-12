package es.iesquevedo.exception;

/**
 * Excepción cuando se intenta un movimiento ilegal
 */
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String message) {
        super(message);
    }

    public InvalidMoveException(String message, Throwable cause) {
        super(message, cause);
    }
}


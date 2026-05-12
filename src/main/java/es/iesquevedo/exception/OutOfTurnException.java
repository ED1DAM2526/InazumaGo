package es.iesquevedo.exception;

/**
 * Excepción cuando se intenta un movimiento fuera de turno
 */
public class OutOfTurnException extends InvalidMoveException {
    public OutOfTurnException(String message) {
        super(message);
    }
}


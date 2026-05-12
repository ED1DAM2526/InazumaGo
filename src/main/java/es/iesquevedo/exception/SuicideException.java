package es.iesquevedo.exception;

/**
 * Excepción cuando se intenta colocar una piedra sin libertades (suicidio)
 */
public class SuicideException extends InvalidMoveException {
    public SuicideException(String message) {
        super(message);
    }
}


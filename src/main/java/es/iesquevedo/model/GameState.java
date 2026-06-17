package es.iesquevedo.model;

/**
 * Estados posibles de una partida.
 */
public enum GameState {
    /**
     * Partida creada, esperando a que se unan jugadores.
     */
    WAITING,

    /**
     * Partida en curso, turnos en progreso.
     */
    IN_PROGRESS,

    /**
     * Partida finalizada por victoria/derrota.
     */
    FINISHED,

    /**
     * Partida abandonada por un jugador.
     */
    ABANDONED
}

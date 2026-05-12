package es.iesquevedo.model.game;

/**
 * Estados posibles de una partida
 */
public enum GameState {
    WAITING("Esperando"),
    PLAYING("En juego"),
    FINISHED("Finalizada");

    private final String displayName;

    GameState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


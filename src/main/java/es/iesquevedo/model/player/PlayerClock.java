package es.iesquevedo.model.player;

/**
 * Reloj de tiempo para cada jugador (3+2: 3 minutos + 2 segundos por movimiento)
 */
public class PlayerClock {
    private static final long INITIAL_TIME_MS = 3 * 60 * 1000; // 3 minutos
    private static final long INCREMENT_MS = 2 * 1000; // 2 segundos

    private long remainingMillis;
    private long turnStartTime;
    private boolean isRunning;

    public PlayerClock() {
        this.remainingMillis = INITIAL_TIME_MS;
        this.isRunning = false;
    }

    /**
     * Inicia el reloj para este jugador
     */
    public void startTurn() {
        turnStartTime = System.currentTimeMillis();
        isRunning = true;
    }

    /**
     * Detiene el reloj y aplica tiempo consumido
     */
    public void endTurn() {
        if (!isRunning) return;
        
        long elapsedMs = System.currentTimeMillis() - turnStartTime;
        remainingMillis = Math.max(0, remainingMillis - elapsedMs + INCREMENT_MS);
        isRunning = false;
    }

    /**
     * Obtiene el tiempo restante en milisegundos
     */
    public long getRemainingMillis() {
        return remainingMillis;
    }

    /**
     * Verifica si el reloj ha expirado
     */
    public boolean isExpired() {
        return remainingMillis <= 0;
    }

    /**
     * Obtiene el tiempo restante en formato MM:SS
     */
    public String getFormattedTime() {
        long totalSeconds = remainingMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Obtiene el tiempo consumido en el turno actual
     */
    public long getCurrentTurnElapsed() {
        if (!isRunning) return 0;
        return System.currentTimeMillis() - turnStartTime;
    }

    public void reset() {
        remainingMillis = INITIAL_TIME_MS;
        isRunning = false;
    }

    @Override
    public String toString() {
        return String.format("PlayerClock{remaining=%s, running=%s}", 
            getFormattedTime(), isRunning);
    }
}


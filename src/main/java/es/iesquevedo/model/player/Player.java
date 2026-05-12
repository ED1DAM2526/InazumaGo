package es.iesquevedo.model.player;

import java.util.Objects;

/**
 * Representa un jugador en la partida
 */
public class Player {
    private final String id;
    private final PlayerColor color;
    private final String displayName;
    private int capturedStones; // prisioneros

    public Player(String id, PlayerColor color, String displayName) {
        this.id = Objects.requireNonNull(id, "Player id cannot be null");
        this.color = Objects.requireNonNull(color, "Player color cannot be null");
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        this.capturedStones = 0;
    }

    public String getId() {
        return id;
    }

    public PlayerColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCapturedStones() {
        return capturedStones;
    }

    public void addCaptures(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Captures cannot be negative");
        }
        this.capturedStones += count;
    }

    public void resetCaptures() {
        this.capturedStones = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return id.equals(player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Player{id='%s', color=%s, name='%s', captured=%d}", 
            id, color, displayName, capturedStones);
    }
}


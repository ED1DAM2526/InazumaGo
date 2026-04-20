package es.iesquevedo.dto;

/**
 * Representa un movimiento individual de un jugador.
 */
public class MoveData {
    private String playerId;
    private String move; // "KICK", "PASS", "TACKLE", etc.
    private Position position;
    private long timestamp;

    // Constructores
    public MoveData() {}

    public MoveData(String playerId, String move, Position position) {
        this.playerId = playerId;
        this.move = move;
        this.position = position;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getMove() { return move; }
    public void setMove(String move) { this.move = move; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "MoveData{" +
                "playerId='" + playerId + '\'' +
                ", move='" + move + '\'' +
                ", position=" + position +
                ", timestamp=" + timestamp +
                '}';
    }
}

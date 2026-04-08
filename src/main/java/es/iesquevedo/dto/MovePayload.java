package es.iesquevedo.dto;

import java.util.List;

/**
 * Representa el payload que se envía cuando un jugador hace movimientos.
 * Se serializa a JSON para enviar a Firebase/API.
 */
public class MovePayload {
    private List<MoveData> moves;
    private long timestamp;
    private String gameVersion; // para evitar conflictos de actualización

    // Constructores
    public MovePayload() {}

    public MovePayload(List<MoveData> moves, long timestamp) {
        this.moves = moves;
        this.timestamp = timestamp;
        this.gameVersion = String.valueOf(System.currentTimeMillis());
    }

    // Getters y Setters
    public List<MoveData> getMoves() { return moves; }
    public void setMoves(List<MoveData> moves) { this.moves = moves; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getGameVersion() { return gameVersion; }
    public void setGameVersion(String gameVersion) { this.gameVersion = gameVersion; }
}

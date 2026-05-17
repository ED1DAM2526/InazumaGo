package es.iesquevedo.dto;

import java.util.List;

public class MovePayload {
    private List<MoveData> moves;
    private long timestamp;
    private int row;      // Fila del movimiento
    private int col;      // Columna del movimiento
    private String playerId;

    // Constructores
    public MovePayload() {}

    public MovePayload(int row, int col, String playerId) {
        this.row = row;
        this.col = col;
        this.playerId = playerId;
        this.timestamp = System.currentTimeMillis();
    }

    public MovePayload(List<MoveData> moves, long timestamp) {
        this.moves = moves;
        this.timestamp = timestamp;
    }

    // Getters y Setters
    public List<MoveData> getMoves() { return moves; }
    public void setMoves(List<MoveData> moves) { this.moves = moves; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
}
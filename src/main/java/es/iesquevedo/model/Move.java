package es.iesquevedo.model;

import java.util.UUID;

/**
 * Representa un movimiento en una partida de Inazuma Go.
 */
public class Move {
    private String id;
    private String playerId;
    private int row;
    private int col;
    private boolean isPass; // true si es un pase, false si es colocar piedra
    private long timestamp;
    private int capturedCount; // número de piedras capturadas en este movimiento

    public Move(String playerId, int row, int col) {
        this.id = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        this.isPass = false;
        this.timestamp = System.currentTimeMillis();
        this.capturedCount = 0;
    }

    public Move(String playerId, boolean isPass) {
        this.id = UUID.randomUUID().toString();
        this.playerId = playerId;
        this.isPass = isPass;
        this.timestamp = System.currentTimeMillis();
        this.capturedCount = 0;
        this.row = -1;
        this.col = -1;
    }

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isPass() {
        return isPass;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getCapturedCount() {
        return capturedCount;
    }

    public void setCapturedCount(int count) {
        this.capturedCount = count;
    }

    @Override
    public String toString() {
        if (isPass) {
            return "Move{" + "playerId='" + playerId + '\'' + ", PASS" + ", timestamp=" + timestamp + '}';
        }
        return "Move{" + "playerId='" + playerId + '\'' + ", row=" + row + ", col=" + col + ", timestamp=" + timestamp + '}';
    }
}

package es.iesquevedo.dto;

/**
 * DTO para sincronizar movimientos remotos durante una partida multijugador.
 * Incluye información del jugador que realiza el movimiento y timestamp.
 */
public class RemoteMoveDto {
    private String moveId;
    private String gameId;
    private String playerId;
    private String playerName;
    private int row;
    private int col;
    private boolean isPass;
    private long timestamp;
    private int turnNumber;
    private String status; // "pending", "confirmed", "rejected"
    private String reason; // razón si fue rechazado

    public RemoteMoveDto() {}

    public RemoteMoveDto(String gameId, String playerId, int row, int col) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        this.isPass = false;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }

    // Getters y Setters
    public String getMoveId() { return moveId; }
    public void setMoveId(String moveId) { this.moveId = moveId; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; }

    public int getCol() { return col; }
    public void setCol(int col) { this.col = col; }

    public boolean isPass() { return isPass; }
    public void setPass(boolean pass) { isPass = pass; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}


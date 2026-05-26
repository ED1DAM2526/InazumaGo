package es.iesquevedo.dto;

/**
 * DTO para rastrear la presencia de jugadores en línea durante una partida.
 */
public class PlayerPresenceDto {
    private String playerId;
    private String playerName;
    private boolean connected;
    private long lastSeenTimestamp;
    private String deviceInfo;

    public PlayerPresenceDto() {}

    public PlayerPresenceDto(String playerId, String playerName, boolean connected) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.connected = connected;
        this.lastSeenTimestamp = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public long getLastSeenTimestamp() { return lastSeenTimestamp; }
    public void setLastSeenTimestamp(long lastSeenTimestamp) { this.lastSeenTimestamp = lastSeenTimestamp; }

    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}


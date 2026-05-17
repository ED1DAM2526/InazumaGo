package es.iesquevedo.dto;

public class MoveRequestDto {
    private String gameId;
    private String playerId;
    private String clientNonce;
    private MovePayload payload;

    public MoveRequestDto() {}

    public MoveRequestDto(String gameId, String playerId, String clientNonce, MovePayload payload) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.clientNonce = clientNonce;
        this.payload = payload;
    }

    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getClientNonce() { return clientNonce; }
    public void setClientNonce(String clientNonce) { this.clientNonce = clientNonce; }

    public MovePayload getPayload() { return payload; }
    public void setPayload(MovePayload payload) { this.payload = payload; }
}
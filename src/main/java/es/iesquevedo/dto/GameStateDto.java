package es.iesquevedo.dto;

import es.iesquevedo.model.BoardState;

import java.util.List;

public class GameStateDto {
    private String gameId;
    private List<String> players;
    private String currentTurnPlayerId;
    private String winnerId;
    private String status; // WAITING, IN_PROGRESS, FINISHED
    private List<MoveData> moves;

    public GameStateDto() {}

    public GameStateDto(String gameId, List<String> players, String currentTurnPlayerId, String status) {
        this.gameId = gameId;
        this.players = players;
        this.currentTurnPlayerId = currentTurnPlayerId;
        this.status = status;
    }

    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public List<String> getPlayers() { return players; }
    public void setPlayers(List<String> players) { this.players = players; }

    public String getCurrentTurnPlayerId() { return currentTurnPlayerId; }
    public void setCurrentTurnPlayerId(String currentTurnPlayerId) { this.currentTurnPlayerId = currentTurnPlayerId; }

    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<MoveData> getMoves() { return moves; }
    public void setMoves(List<MoveData> moves) { this.moves = moves; }

    private BoardState boardState;

    // Add getter and setter
    public BoardState getBoardState() { return boardState; }
    public void setBoardState(BoardState boardState) { this.boardState = boardState; }
}
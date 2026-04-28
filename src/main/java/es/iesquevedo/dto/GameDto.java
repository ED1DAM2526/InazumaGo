package es.iesquevedo.dto;

import java.util.List;

public class GameDto {
    private String id;
    private String name;
    private List<String> players;
    private String status; // "WAITING", "IN_PROGRESS", "FINISHED"
    private long createdAt;
    private List<MoveData> moves;

    // Constructores
    public GameDto() {}

    public GameDto(String id, String name, List<String> players, String status, long createdAt) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getPlayers() { return players; }
    public void setPlayers(List<String> players) { this.players = players; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<MoveData> getMoves() { return moves; }
    public void setMoves(List<MoveData> moves) { this.moves = moves; }

}

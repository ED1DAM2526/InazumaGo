package es.iesquevedo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private String id;
    private String name;
    private List<Player> players;
    private GameState state;
    private int currentPlayerIndex;
    private int turnCount;
    private String winnerPlayerId;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    public Game(String name, Player player1) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.players = new ArrayList<>();
        this.players.add(player1);
        this.state = GameState.WAITING;
        this.currentPlayerIndex = 0;
        this.turnCount = 0;
        this.winnerPlayerId = null;
        this.createdAt = LocalDateTime.now();
        this.finishedAt = null;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Player> getPlayers() { return players; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentPlayerIndex);
    }
    public int getTurnCount() { return turnCount; }
    public void incrementTurnCount() { this.turnCount++; }
    public String getWinnerPlayerId() { return winnerPlayerId; }
    public void setWinnerPlayerId(String playerId) { this.winnerPlayerId = playerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }

    public void addPlayer(Player player) {
        if (state != GameState.WAITING) {
            throw new IllegalStateException("No se pueden añadir jugadores");
        }
        if (players.size() >= 2) {
            throw new IllegalStateException("Ya hay 2 jugadores");
        }
        players.add(player);
    }

    public void start() {
        if (players.size() < 2) {
            throw new IllegalStateException("Se necesitan 2 jugadores");
        }
        this.state = GameState.IN_PROGRESS;
        this.currentPlayerIndex = 0;
    }

    public void nextTurn() {
        if (state != GameState.IN_PROGRESS) {
            throw new IllegalStateException("No se puede avanzar turno");
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        incrementTurnCount();
    }

    public void finish(String winnerPlayerId) {
        this.state = GameState.FINISHED;
        this.winnerPlayerId = winnerPlayerId;
        this.finishedAt = LocalDateTime.now();
    }

    public void abandon() {
        this.state = GameState.ABANDONED;
        this.finishedAt = LocalDateTime.now();
    }
}


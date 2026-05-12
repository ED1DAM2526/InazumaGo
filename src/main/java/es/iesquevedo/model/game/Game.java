package es.iesquevedo.model.game;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import es.iesquevedo.model.player.PlayerClock;

import java.util.*;

/**
 * Aggregado raíz: representa una partida de Inazuma Go
 */
public class Game {
    private final String gameId;
    private final Board board;
    private final Map<PlayerColor, Player> players;
    private final List<Move> moveHistory;
    private final List<Board> boardHistory; // Para Ko
    private final Map<PlayerColor, PlayerClock> clocks;
    
    private GameState state;
    private PlayerColor currentPlayer; // Turno
    private int passCount; // Contador de pases consecutivos
    private int movesWithoutCapture; // Para límite de 8 sin captura
    private GameResult result;
    
    private static final int BOARD_SIZE = 9;
    private static final int NO_CAPTURE_LIMIT = 8;
    private static final int MIN_MOVES_FOR_NO_CAPTURE = 20;

    public Game(String gameId) {
        this.gameId = gameId;
        this.board = new Board();
        this.players = new HashMap<>();
        this.moveHistory = new ArrayList<>();
        this.boardHistory = new ArrayList<>();
        this.clocks = new HashMap<>();
        this.state = GameState.WAITING;
        this.passCount = 0;
        this.movesWithoutCapture = 0;
        this.currentPlayer = PlayerColor.BLACK; // Negro empieza
    }

    // GETTERS
    public String getGameId() { return gameId; }
    public Board getBoard() { return board; }
    public GameState getState() { return state; }
    public PlayerColor getCurrentPlayer() { return currentPlayer; }
    public Player getPlayer(PlayerColor color) { return players.get(color); }
    public List<Move> getMoveHistory() { return Collections.unmodifiableList(moveHistory); }
    public int getMoveCount() { return moveHistory.size(); }
    public GameResult getResult() { return result; }

    // SETTERS
    public void setState(GameState state) { this.state = state; }
    public void setResult(GameResult result) { this.result = result; }

    /**
     * Añade un jugador a la partida
     */
    public void addPlayer(Player player) {
        if (players.size() >= 2) {
            throw new IllegalStateException("Game already has 2 players");
        }
        players.put(player.getColor(), player);
        clocks.put(player.getColor(), new PlayerClock());
    }

    /**
     * Obtiene el jugador contrario
     */
    public Player getOpponent(PlayerColor color) {
        return players.get(color.opponent());
    }

    /**
     * Inicia la partida
     */
    public void startGame() {
        if (players.size() != 2) {
            throw new IllegalStateException("Need exactly 2 players to start");
        }
        state = GameState.PLAYING;
        currentPlayer = PlayerColor.BLACK;
        clocks.get(PlayerColor.BLACK).startTurn();
    }

    /**
     * Registra un movimiento en el historial
     */
    public void recordMove(Move move) {
        moveHistory.add(move);
        boardHistory.add(board.copy());
    }

    /**
     * Cambia el turno al siguiente jugador
     */
    public void nextTurn() {
        // Detener reloj del jugador actual
        clocks.get(currentPlayer).endTurn();
        
        // Cambiar turno
        currentPlayer = currentPlayer.opponent();
        
        // Iniciar reloj del nuevo jugador
        clocks.get(currentPlayer).startTurn();
        
        // Comprobar tiempo expirado
        if (clocks.get(currentPlayer).isExpired()) {
            endGameByTime(currentPlayer.opponent());
        }
    }

    /**
     * Maneja un pase
     */
    public void handlePass() {
        passCount++;
        
        if (passCount >= 2) {
            // Doble pase: finalizar partida
            endGameByDoublePasse();
        }
    }

    /**
     * Resetea contador de pases (cuando hay movimiento real)
     */
    public void resetPassCount() {
        passCount = 0;
    }

    /**
     * Actualiza contador de movimientos sin captura
     */
    public void updateNoCaptureMoves(boolean hasCaptured) {
        if (hasCaptured) {
            movesWithoutCapture = 0;
        } else {
            movesWithoutCapture++;
            
            // Comprobar límite: 8 movimientos sin captura después del movimiento 20
            if (moveHistory.size() > MIN_MOVES_FOR_NO_CAPTURE && 
                movesWithoutCapture >= NO_CAPTURE_LIMIT) {
                endGameByNoCaptureLimit();
            }
        }
    }

    /**
     * Finalizar por doble pase
     */
    public void endGameByDoublePasse() {
        state = GameState.FINISHED;
        if (result == null) {
            result = new GameResult(
                PlayerColor.BLACK, 0, 0, 0,
                "Double Pass"
            );
        }
    }

    /**
     * Finalizar por tiempo
     */
    public void endGameByTime(PlayerColor winner) {
        state = GameState.FINISHED;
        result = new GameResult(winner, 0, 0, 0, "Time");
    }

    /**
     * Finalizar por límite sin capturas
     */
    public void endGameByNoCaptureLimit() {
        state = GameState.FINISHED;
        if (result == null) {
            result = new GameResult(
                PlayerColor.BLACK, 0, 0, 0,
                "No Capture Limit"
            );
        }
    }

    /**
     * Finalizar por Ko
     */
    public void endGameByKo() {
        state = GameState.FINISHED;
        if (result == null) {
            result = new GameResult(
                PlayerColor.BLACK, 0, 0, 0,
                "Ko"
            );
        }
    }

    /**
     * Obtiene el historial de tableros
     */
    public List<Board> getBoardHistory() {
        return Collections.unmodifiableList(boardHistory);
    }

    /**
     * Verifica si la partida está activa
     */
    public boolean isActive() {
        return state == GameState.PLAYING;
    }

    /**
     * Obtiene reloj del jugador
     */
    public PlayerClock getClock(PlayerColor color) {
        return clocks.get(color);
    }

    @Override
    public String toString() {
        return String.format("Game{id=%s, state=%s, moves=%d, current=%s}", 
            gameId, state, moveHistory.size(), currentPlayer);
    }
}


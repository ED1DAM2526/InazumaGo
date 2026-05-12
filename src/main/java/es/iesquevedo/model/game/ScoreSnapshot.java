package es.iesquevedo.model.game;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.player.PlayerColor;

import java.util.Objects;

/**
 * Captura de puntuación en un momento específico
 */
public class ScoreSnapshot {
    private final int blackScore;
    private final int whiteScore;
    private final int blackTerritory;
    private final int whiteTerritory;
    private final long timestamp;

    public ScoreSnapshot(int blackScore, int whiteScore, 
                        int blackTerritory, int whiteTerritory) {
        this.blackScore = blackScore;
        this.whiteScore = whiteScore;
        this.blackTerritory = blackTerritory;
        this.whiteTerritory = whiteTerritory;
        this.timestamp = System.currentTimeMillis();
    }

    public int getBlackScore() {
        return blackScore;
    }

    public int getWhiteScore() {
        return whiteScore;
    }

    public int getBlackTerritory() {
        return blackTerritory;
    }

    public int getWhiteTerritory() {
        return whiteTerritory;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public PlayerColor getLeader() {
        if (blackScore > whiteScore) return PlayerColor.BLACK;
        if (whiteScore > blackScore) return PlayerColor.WHITE;
        return null;
    }

    public int getPointsDifference() {
        return Math.abs(blackScore - whiteScore);
    }

    @Override
    public String toString() {
        return String.format("Score{B=%d, W=%d, diff=%d}", 
            blackScore, whiteScore, getPointsDifference());
    }
}


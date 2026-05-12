package es.iesquevedo.model.game;

import es.iesquevedo.model.player.PlayerColor;

import java.util.Objects;

/**
 * Resultado final de una partida
 */
public class GameResult {
    private final PlayerColor winner;
    private final int pointsDifference;
    private final int blackScore;
    private final int whiteScore;
    private final String reason; // "Score", "Time", "Ko", "Double Pass", "No Capture Limit"

    public GameResult(PlayerColor winner, int pointsDifference, 
                     int blackScore, int whiteScore, String reason) {
        this.winner = Objects.requireNonNull(winner);
        this.pointsDifference = pointsDifference;
        this.blackScore = blackScore;
        this.whiteScore = whiteScore;
        this.reason = Objects.requireNonNull(reason);
    }

    public PlayerColor getWinner() {
        return winner;
    }

    public int getPointsDifference() {
        return pointsDifference;
    }

    public int getBlackScore() {
        return blackScore;
    }

    public int getWhiteScore() {
        return whiteScore;
    }

    public String getReason() {
        return reason;
    }

    /**
     * Obtiene descripción legible del resultado
     */
    public String getDescription() {
        return String.format(
            "%s gana por %d punto%s (%d-%d) - %s",
            winner.getDisplayName(),
            pointsDifference,
            pointsDifference == 1 ? "" : "s",
            winner == PlayerColor.BLACK ? blackScore : whiteScore,
            winner == PlayerColor.BLACK ? whiteScore : blackScore,
            reason
        );
    }

    @Override
    public String toString() {
        return getDescription();
    }
}


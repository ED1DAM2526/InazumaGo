package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Group;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de ejecutar un movimiento
 */
public class MoveResult {
    private final Move move;
    private final boolean valid;
    private final String rejectReason;
    private final List<Group> capturedGroups;
    private final int capturedStoneCount;
    private final boolean captured;

    public MoveResult(Move move, List<Group> capturedGroups) {
        this.move = Objects.requireNonNull(move);
        this.valid = true;
        this.rejectReason = null;
        this.capturedGroups = Objects.requireNonNull(capturedGroups);
        this.capturedStoneCount = capturedGroups.stream()
            .mapToInt(g -> g.getSize())
            .sum();
        this.captured = !capturedGroups.isEmpty();
    }

    public MoveResult(Move move, String rejectReason) {
        this.move = Objects.requireNonNull(move);
        this.valid = false;
        this.rejectReason = Objects.requireNonNull(rejectReason);
        this.capturedGroups = List.of();
        this.capturedStoneCount = 0;
        this.captured = false;
    }

    public Move getMove() {
        return move;
    }

    public boolean isValid() {
        return valid;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public List<Group> getCapturedGroups() {
        return capturedGroups;
    }

    public int getCapturedStoneCount() {
        return capturedStoneCount;
    }

    public boolean hasCaptured() {
        return captured;
    }

    @Override
    public String toString() {
        if (!valid) {
            return String.format("MoveResult{REJECTED: %s}", rejectReason);
        }
        return String.format("MoveResult{VALID, captured=%d}", capturedStoneCount);
    }
}

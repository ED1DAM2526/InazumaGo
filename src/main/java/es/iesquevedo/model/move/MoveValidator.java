package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.OutOfTurnException;
import es.iesquevedo.exception.SuicideException;

import java.util.List;

/**
 * Valida si un movimiento es legal según las reglas de Inazuma Go
 */
public class MoveValidator {
    private final SuicideDetector suicideDetector = new SuicideDetector();
    private final KoDetector koDetector = new KoDetector();
    private final BoardAnalyzer analyzer = new BoardAnalyzer();

    /**
     * Valida completamente un movimiento
     */
    public ValidationResult validate(Move move, Board board, PlayerColor currentPlayerColor, 
                                    List<Board> boardHistory) {
        // 1. Validar que es turno del jugador
        if (move.getActor() != currentPlayerColor) {
            return ValidationResult.invalid(
                String.format("No es tu turno. Le toca a %s", currentPlayerColor.getDisplayName())
            );
        }

        // 2. Si es pase, es siempre válido
        if (move.isPass()) {
            return ValidationResult.valid(List.of());
        }

        // 3. Validar posición libre
        if (!board.isEmpty(move.getPosition())) {
            return ValidationResult.invalid(
                String.format("La posición %s ya está ocupada", move.getPosition().toGoNotation())
            );
        }

        // 4. Detectar suicidio
        if (suicideDetector.isSuicide(move, board)) {
            return ValidationResult.invalid("Movimiento de suicidio no permitido");
        }

        // 5. Detectar Ko
        if (koDetector.isKoMove(move, board, boardHistory)) {
            return ValidationResult.invalid(
                "Ko detectado: no se puede recapturar inmediatamente"
            );
        }

        // 6. Calcular capturas (el movimiento es válido)
        Board temp = board.copy();
        Stone stone = move.getActor() == es.iesquevedo.model.player.PlayerColor.BLACK 
            ? Stone.BLACK : Stone.WHITE;
        temp.placeStone(move.getPosition(), stone);
        
        List<Group> capturedGroups = analyzer.findCapturedGroupsAdjacentTo(temp, move.getPosition());

        return ValidationResult.valid(capturedGroups);
    }

    /**
     * Resultado de validación
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String reason;
        private final List<Group> capturedGroups;

        private ValidationResult(boolean valid, String reason, List<Group> capturedGroups) {
            this.valid = valid;
            this.reason = reason;
            this.capturedGroups = capturedGroups;
        }

        public static ValidationResult valid(List<Group> capturedGroups) {
            return new ValidationResult(true, null, capturedGroups);
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason, List.of());
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }

        public List<Group> getCapturedGroups() {
            return capturedGroups;
        }

        @Override
        public String toString() {
            return valid ? "VALID" : "INVALID: " + reason;
        }
    }
}

// Imports needed
import es.iesquevedo.model.player.PlayerColor;
import es.iesquevedo.model.board.Group;


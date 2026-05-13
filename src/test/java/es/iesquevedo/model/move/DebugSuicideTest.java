package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.PlayerColor;
import org.junit.jupiter.api.Test;

public class DebugSuicideTest {
    @Test
    public void debugSuicideScenario() {
        Board board = new Board();
        board.placeStone(Position.of(3,4), Stone.WHITE);
        board.placeStone(Position.of(5,4), Stone.WHITE);
        board.placeStone(Position.of(4,3), Stone.WHITE);
        board.placeStone(Position.of(4,5), Stone.WHITE);

        System.out.println("Board snapshot around center:");
        for (int r = 2; r <= 6; r++) {
            for (int c = 2; c <= 6; c++) {
                System.out.print(board.getStone(Position.of(r,c)).getSymbol());
            }
            System.out.println();
        }

        Move move = new Move(Position.of(4,4), PlayerColor.BLACK, "dbg");
        MoveValidator v = new MoveValidator();
        var res = v.validate(move, board, PlayerColor.BLACK, java.util.List.of());
        System.out.println("Validation result: " + res.isValid() + ", reason=" + res.getReason());
    }
}

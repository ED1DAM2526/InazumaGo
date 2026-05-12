package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Group;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.PlayerColor;

import java.util.List;

/**
 * Detecta si una jugada es suicidio (la piedra propia queda sin libertades)
 */
public class SuicideDetector {
    private final BoardAnalyzer analyzer = new BoardAnalyzer();

    /**
     * Detecta si un movimiento es suicidio
     * Suicidio = la piedra colocada queda sin libertades Y no captura a ningún grupo enemigo
     */
    public boolean isSuicide(Move move, Board board) {
        if (move.isPass()) {
            return false; // Un pase nunca es suicidio
        }

        Board temp = board.copy();
        Stone stone = move.getActor() == PlayerColor.BLACK 
            ? Stone.BLACK : Stone.WHITE;
        
        temp.placeStone(move.getPosition(), stone);

        // Comprobar si hay capturas enemigas
        List<Group> capturedEnemyGroups = analyzer.findCapturedGroupsAdjacentTo(temp, move.getPosition());
        if (!capturedEnemyGroups.isEmpty()) {
            // Hay capturas: no es suicidio
            return false;
        }

        // Sin capturas: comprobar si el grupo propio tiene libertades
        Group ownGroup = analyzer.findGroupAt(temp, move.getPosition());
        return ownGroup != null && ownGroup.countLiberties(temp) == 0;
    }
}


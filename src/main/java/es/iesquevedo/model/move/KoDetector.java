package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Group;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.player.PlayerColor;

import java.util.List;

/**
 * Detecta repeticiones de Ko según reglas de Inazuma Go
 * Ko detectado = misma posición que el turno inmediatamente anterior
 */
public class KoDetector {

    /**
     * Detecta si aplicar este movimiento resultaría en Ko (repetición consecutiva)
     * Compara con el historial de tableros
     */
    public boolean isKoMove(Move move, Board currentBoard, List<Board> boardHistory) {
        if (boardHistory.isEmpty() || move.isPass()) {
            return false;
        }

        // Simular el movimiento
        Board temp = currentBoard.copy();
        Stone stone = move.getActor() == es.iesquevedo.model.player.PlayerColor.BLACK 
            ? Stone.BLACK : Stone.WHITE;
        
        temp.placeStone(move.getPosition(), stone);

        // Buscar capturas y eliminarlas
        BoardAnalyzer analyzer = new BoardAnalyzer();
        List<Group> captured = analyzer.findCapturedGroupsAdjacentTo(temp, move.getPosition());
        for (Group group : captured) {
            for (Position pos : group.getStones()) {
                temp.removeStone(pos);
            }
        }

        // Comparar con el tablero anterior (penúltimo en el historial)
        if (boardHistory.size() >= 1) {
            Board previousBoard = boardHistory.get(boardHistory.size() - 1);
            if (temp.equals(previousBoard)) {
                return true; // Ko detectado: misma posición que hace 1 turno
            }
        }

        return false;
    }

    /**
     * Verifica si dos tableros son idénticos
     */
    public boolean boardsEqual(Board board1, Board board2) {
        return board1.equals(board2);
    }

    // Implementación de Ko detector (placeholder minimal) - evitar código fuera de la clase

    public boolean isKoSituation(String[][] board, int row, int col, String player) {
        // Implementación mínima: por ahora no bloquear movimientos (puede mejorarse)
        return false;
    }
}

package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Group;
import es.iesquevedo.model.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MoveExecutor {

    /**
     * Ejecuta el movimiento sobre una copia del tablero y devuelve el tablero resultante.
     * (API existente, mantiene comportamiento)
     */
    public Board executeMove(Board board, Move move) {
        Board copy = board.copy();

        if (move.isPass()) return copy;

        Stone stone = move.getActor().toStone();
        copy.placeStone(move.getPosition(), stone);

        // Capturas simples: usar BoardAnalyzer para encontrar grupos capturados
        BoardAnalyzer analyzer = new BoardAnalyzer();
        List<Group> captured = analyzer.findCapturedGroups(copy);
        for (Group group : captured) {
            for (Position pos : group.getStones()) {
                copy.removeStone(pos);
            }
        }

        return copy;
    }

    /**
     * Ejecuta movimiento con contexto de jugadores y devuelve MoveResult con grupos capturados.
     * Ahora modifica el tablero proporcionado (side-effect) y actualiza prisioneros.
     */
    public MoveResult executeMove(Move move, Board board, Player currentPlayer, Player opponent) {
        if (move.isPass()) {
            // No change
            return new MoveResult(move, new ArrayList<>());
        }

        Stone stone = move.getActor().toStone();
        // Colocar en el tablero real
        board.placeStone(move.getPosition(), stone);

        BoardAnalyzer analyzer = new BoardAnalyzer();
        List<Group> captured = analyzer.findCapturedGroups(board);

        int totalCaptured = 0;
        for (Group g : captured) {
            totalCaptured += g.getSize();
            for (Position p : g.getStones()) board.removeStone(p);
        }

        // Actualizar contador de prisioneros en el jugador oponente
        if (opponent != null && totalCaptured > 0) {
            opponent.addCaptures(totalCaptured);
        }

        return new MoveResult(move, captured);
    }
}

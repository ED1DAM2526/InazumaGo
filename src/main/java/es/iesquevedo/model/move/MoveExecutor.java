package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Group;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.Player;

import java.util.List;

/**
 * Ejecutor de movimientos: aplica un movimiento válido al tablero
 */
public class MoveExecutor {
    private final BoardAnalyzer analyzer = new BoardAnalyzer();

    /**
     * Ejecuta un movimiento validado al tablero
     * Retorna el resultado con capturas realizadas
     */
    public MoveResult executeMove(Move move, Board board, Player currentPlayer, 
                                  Player opponentPlayer) {
        if (move.isPass()) {
            // Pase: no cambia el tablero
            return new MoveResult(move, List.of());
        }

        // Colocar piedra
        Stone stone = currentPlayer.getColor() == es.iesquevedo.model.player.PlayerColor.BLACK 
            ? Stone.BLACK : Stone.WHITE;
        board.placeStone(move.getPosition(), stone);

        // Detectar y eliminar capturas
        List<Group> capturedGroups = analyzer.findCapturedGroupsAdjacentTo(board, move.getPosition());
        
        for (Group group : capturedGroups) {
            for (Position pos : group.getStones()) {
                board.removeStone(pos);
            }
            // Incrementar prisioneros del jugador contrario
            opponentPlayer.addCaptures(group.getSize());
        }

        return new MoveResult(move, capturedGroups);
    }
}

// Imports needed
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.player.PlayerColor;


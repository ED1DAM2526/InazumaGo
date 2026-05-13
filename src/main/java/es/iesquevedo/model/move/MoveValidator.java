package es.iesquevedo.model.move;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.player.PlayerColor;

import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Valida si un movimiento es legal según las reglas de Inazuma Go
 */
public class MoveValidator {
    private static final Logger LOG = Logger.getLogger(MoveValidator.class.getName());

    /**
     * Valida completamente un movimiento
     */
    public boolean isLegal(Board board, Move move) {
        // 1. Si es pase, es siempre válido
        if (move.isPass()) return true;

        Position p = move.getPosition();
        int size = board.getSize();

        // 2. Comprobar límites del tablero
        if (p.getRow() < 0 || p.getRow() >= size || p.getCol() < 0 || p.getCol() >= size) return false;

        // 3. Comprobar celda vacía
        if (!board.isEmpty(p)) return false; // celda ocupada

        // 4. Comprobación de suicidio básica: permitir por ahora
        return true;
    }

    private boolean groupHasLiberties(Board board, Position start) {
        Stone color = board.getStone(start);
        if (!color.isStone()) return false;
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        ArrayDeque<Position> q = new ArrayDeque<>();
        q.add(start);
        visited[start.getRow()][start.getCol()] = true;

        while (!q.isEmpty()) {
            Position cur = q.poll();
            for (Position n : cur.getOrthogonalNeighbors()) {
                Stone s = board.getStone(n);
                System.out.println("DEBUG: neighbor=" + n + " stone=" + s);
                LOG.info("Checking neighbor " + n + " stone=" + s);
                if (visited[n.getRow()][n.getCol()]) continue;
                if (s == Stone.EMPTY) {
                    System.out.println("DEBUG: found liberty at " + n);
                    LOG.info("Found liberty at " + n);
                    return true; // found liberty
                }
                if (s == color) {
                    visited[n.getRow()][n.getCol()] = true;
                    q.add(n);
                }
            }
        }
        return false;
    }

    /**
     * Nueva API: validar con más contexto y devolver ValidationResult
     */
    public ValidationResult validate(Move move, Board board, PlayerColor currentPlayer, java.util.List<Board> history) {
        // Compruebas detalladas para mensajes
        if (move.isPass()) return ValidationResult.ok();

        Position p = move.getPosition();
        int size = board.getSize();
        if (p.getRow() < 0 || p.getRow() >= size || p.getCol() < 0 || p.getCol() >= size) {
            LOG.info("Movimiento fuera de límites: " + p);
            return ValidationResult.fail("Movimiento ilegal");
        }
        if (!board.isEmpty(p)) {
            LOG.info("Movimiento en posición ocupada: " + p);
            return ValidationResult.fail("ocupada");
        }

        // Validación de turno
        if (!move.getActor().equals(currentPlayer)) {
            LOG.info("Movimiento fuera de turno: actor=" + move.getActor() + ", current=" + currentPlayer);
            return ValidationResult.fail("fuera de turno");
        }

        // Simular movimiento
        Board temp = board.copy();
        temp.placeStone(move.getPosition(), move.getActor().toStone());

        // Detectar capturas manualmente: comprobar grupos enemigos adyacentes a la jugada
        Stone opponent = move.getActor().toStone().opponent();
        BoardAnalyzer analyzer = new BoardAnalyzer();
        List<es.iesquevedo.model.board.Group> capturedGroups = new ArrayList<>();
        for (Position neighbor : move.getPosition().getOrthogonalNeighbors()) {
            if (!temp.isEmpty(neighbor) && temp.getStone(neighbor) == opponent) {
                // si el grupo enemigo en 'neighbor' no tiene libertades -> será capturado
                if (!groupHasLiberties(temp, neighbor)) {
                    var g = analyzer.findGroupAt(temp, neighbor);
                    if (g != null) capturedGroups.add(g);
                }
            }
        }

        if (!capturedGroups.isEmpty()) {
            LOG.info("Movimiento captura: " + capturedGroups.size() + " grupos");
            return ValidationResult.okWithCaptures(capturedGroups);
        }

        // suicidio: comprobar con BFS si el grupo en la posición de la jugada tiene libertades
        boolean hasLib = groupHasLiberties(temp, move.getPosition());
        LOG.info("Group has liberties after move: " + hasLib + " at " + move.getPosition());

        // Heurística adicional: si no hay capturas y todos los vecinos existentes son del oponente => suicidio
        if (!capturedGroups.isEmpty()) {
            // already handled
        } else {
            int neighborCount = 0;
            int opponentNeighbors = 0;
            Stone opp = move.getActor().toStone().opponent();
            for (Position n : move.getPosition().getOrthogonalNeighbors()) {
                neighborCount++;
                if (!temp.isEmpty(n) && temp.getStone(n) == opp) opponentNeighbors++;
            }
            if (neighborCount > 0 && opponentNeighbors == neighborCount) {
                LOG.info("Heurística: todos los vecinos son del oponente -> suicidio");
                hasLib = false;
            }
        }

        if (!hasLib) {
            LOG.info("Movimiento suicida detectado en: " + move.getPosition());
            return ValidationResult.fail("suicidio");
        }

        // Ko
        if (history != null && !history.isEmpty()) {
            for (Board past : history) {
                if (past.equals(temp)) {
                    LOG.info("Movimiento viola Ko (repetición de tablero)");
                    return ValidationResult.fail("Ko");
                }
            }
        }

        LOG.info("Movimiento válido: " + move);
        return ValidationResult.ok();
    }
}

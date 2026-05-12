package es.iesquevedo.model.scoring;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.board.BoardAnalyzer;
import es.iesquevedo.model.board.Stone;
import es.iesquevedo.model.game.GameResult;
import es.iesquevedo.model.game.ScoreSnapshot;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;

import java.util.List;

/**
 * Scorer: calcula puntuación según reglas chinas de Inazuma Go
 */
public class ChineseScorerImpl {
    private static final double KOMI = 5.5; // Ventaja blanca

    /**
     * Calcula la puntuación provisional en cualquier momento
     */
    public ScoreSnapshot calculateProvisionalScore(Board board, Player blackPlayer, Player whitePlayer) {
        // 1. Contar piedras en tablero
        int blackStones = board.countStones(Stone.BLACK);
        int whiteStones = board.countStones(Stone.WHITE);

        // 2. Encontrar territorios
        BoardAnalyzer analyzer = new BoardAnalyzer();
        List<BoardAnalyzer.Territory> territories = analyzer.findTerritories(board);

        // 3. Contar territorio
        int blackTerritory = 0;
        int whiteTerritory = 0;

        for (BoardAnalyzer.Territory territory : territories) {
            if (territory.getAdjacentColors().size() == 1) {
                // Territorio exclusivo
                Stone owner = territory.getAdjacentColors().iterator().next();
                if (owner == Stone.BLACK) {
                    blackTerritory += territory.getPositions().size();
                } else if (owner == Stone.WHITE) {
                    whiteTerritory += territory.getPositions().size();
                }
            }
            // Si 2 colores o 0: es neutro, no cuenta
        }

        // 4. Calcular puntuación total
        int blackScore = blackStones + blackTerritory + blackPlayer.getCapturedStones();
        int whiteScore = (int)(whiteStones + whiteTerritory + whitePlayer.getCapturedStones() + KOMI);

        return new ScoreSnapshot(blackScore, whiteScore, blackTerritory, whiteTerritory);
    }

    /**
     * Calcula la puntuación final de la partida
     */
    public GameResult calculateFinalScore(Board finalBoard, Player blackPlayer, 
                                         Player whitePlayer, String reason) {
        // 1. Limpiar tablero: eliminar grupos sin libertades
        Board cleaned = cleanupBoard(finalBoard);

        // 2. Calcular puntuación en tablero limpio
        ScoreSnapshot snapshot = calculateProvisionalScore(cleaned, blackPlayer, whitePlayer);

        // 3. Determinar ganador
        PlayerColor winner;
        int pointsDifference;

        if (snapshot.getBlackScore() > snapshot.getWhiteScore()) {
            winner = PlayerColor.BLACK;
            pointsDifference = snapshot.getBlackScore() - snapshot.getWhiteScore();
        } else {
            winner = PlayerColor.WHITE;
            pointsDifference = snapshot.getWhiteScore() - snapshot.getBlackScore();
        }

        return new GameResult(winner, pointsDifference, 
                            snapshot.getBlackScore(), snapshot.getWhiteScore(), reason);
    }

    /**
     * Limpia el tablero eliminando grupos sin libertades
     */
    private Board cleanupBoard(Board board) {
        Board cleaned = board.copy();
        BoardAnalyzer analyzer = new BoardAnalyzer();

        boolean changed = true;
        while (changed) {
            List<es.iesquevedo.model.board.Group> captured = analyzer.findCapturedGroups(cleaned);
            changed = !captured.isEmpty();

            for (es.iesquevedo.model.board.Group group : captured) {
                for (es.iesquevedo.model.board.Position pos : group.getStones()) {
                    cleaned.removeStone(pos);
                }
            }
        }

        return cleaned;
    }
}

// Imports needed
import es.iesquevedo.model.board.Position;


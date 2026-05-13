package es.iesquevedo.model.scoring;

import es.iesquevedo.model.board.Board;
import es.iesquevedo.model.game.ScoreSnapshot;

public interface Scorer {
    ScoreSnapshot computeScore(Board board);
}

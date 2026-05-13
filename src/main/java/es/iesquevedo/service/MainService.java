package es.iesquevedo.service;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.dto.Position;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MainService {
    String greet();

    CompletableFuture<GameDto> getGame(String gameId);

    CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload);

    String addMovesListener(String gameId, Consumer<List<MoveData>> listener);

    /**
     * Inicia o reinicia una nueva partida en memoria.
     */
    void startNewGame();

    /**
     * Intenta hacer un movimiento en la posición dada. Devuelve true si el movimiento se aplicó.
     */
    boolean makeMove(Position position);

    /**
     * Devuelve el tablero actual como una matriz de String. Cada celda es: "X", "O" o null/"".
     */
    String[][] getBoard();

    /**
     * Jugador actual: "X" o "O".
     */
    String getCurrentPlayer();

    /**
     * Devuelve el ganador si existe.
     */
    Optional<String> getWinner();
}

package es.iesquevedo.service;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;

import java.util.concurrent.CompletableFuture;

/**
 * Interfaz para el servicio de eventos de juego.
 * Define operaciones para notificar eventos de partida a Firebase.
 */
public interface GameEventService {

    /**
     * Notifica el inicio de una partida
     *
     * @param gameId ID de la partida
     * @param gameDto Datos de la partida
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    CompletableFuture<Void> notifyGameStart(String gameId, GameDto gameDto);

    /**
     * Notifica un movimiento durante la partida
     *
     * @param gameId ID de la partida
     * @param moveData Datos del movimiento
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    CompletableFuture<Void> notifyGameMove(String gameId, MoveData moveData);

    /**
     * Notifica el fin de una partida
     *
     * @param gameId ID de la partida
     * @param gameDto Datos finales de la partida
     * @return CompletableFuture que se completa cuando el evento se haya sincronizado
     */
    CompletableFuture<Void> notifyGameEnd(String gameId, GameDto gameDto);

    /**
     * Detiene el servicio y libera recursos
     */
    void shutdown();
}


package es.iesquevedo.repository;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Contrato para el repositorio que gestiona partidas (Games) y movimientos (Moves).
 * Puede trabajar contra Firebase Realtime DB (REST) o una implementación en memoria.
 */
public interface MainRepository {

    /**
     * Obtiene una partida completa por su ID.
     * @param gameId ID único de la partida
     * @return CompletableFuture que resuelve a un GameDto o null si no existe
     */
    CompletableFuture<GameDto> getGame(String gameId);

    /**
     * Escribe movimientos en múltiples paths de una partida.
     * Esto usa una petición PATCH para actualizar varias rutas a la vez.
     *
     * @param gameId ID de la partida
     * @param payload objeto con la estructura de movimientos
     * @return CompletableFuture que resuelve cuando se confirma la escritura
     */
    CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload);

    /**
     * Suscribe un listener a cambios en los movimientos de una partida.
     * @param gameId ID de la partida
     * @param listener función que recibe el array de movimientos actualizado
     * @return ID de la suscripción
     */
    String addMovesListener(String gameId, Consumer<java.util.List<MoveData>> listener);

    /**
     * Desuscribe un listener por su ID.
     * @param gameId ID de la partida
     * @param listenerId ID retornado por addMovesListener()
     */
    void removeMovesListener(String gameId, String listenerId);

    String findDefaultName();

    // ========== NUEVOS MÉTODOS PARA E3-US3 ==========

    /**
     * Crea una nueva partida.
     * @param playerId ID del jugador que crea la partida
     * @return CompletableFuture con el ID de la partida creada
     */
    CompletableFuture<String> createGame(String playerId);

    /**
     * Unirse a una partida existente.
     * @param gameId ID de la partida
     * @param playerId ID del jugador que se une
     * @return CompletableFuture con el estado actualizado de la partida
     */
    CompletableFuture<GameStateDto> joinGame(String gameId, String playerId);

    /**
     * Envía un movimiento en una partida.
     * @param gameId ID de la partida
     * @param playerId ID del jugador que hace el movimiento
     * @param payload datos del movimiento
     * @param clientNonce identificador único para evitar duplicados
     * @return CompletableFuture con el estado actualizado de la partida
     */
    CompletableFuture<GameStateDto> submitMove(String gameId, String playerId, MovePayload payload, String clientNonce);

    /**
     * Obtiene el estado actual de una partida.
     * @param gameId ID de la partida
     * @return CompletableFuture con el estado de la partida
     */
    CompletableFuture<GameStateDto> getGameState(String gameId);
}
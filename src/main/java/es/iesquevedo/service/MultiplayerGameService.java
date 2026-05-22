package es.iesquevedo.service;

import es.iesquevedo.dto.RemoteMoveDto;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Servicio especializado para partidas multijugador con sincronización Firebase.
 * Maneja la creación, unión, sincronización de movimientos y estado en tiempo real.
 */
public interface MultiplayerGameService {

    /**
     * Crea una nueva partida multijugador en Firebase.
     *
     * @param gameName nombre de la partida
     * @param player1 primer jugador (creador)
     * @return CompletableFuture con el ID de la partida creada
     */
    CompletableFuture<String> createMultiplayerGame(String gameName, Player player1);

    /**
     * Se une a una partida existente como segundo jugador.
     *
     * @param gameId ID de la partida
     * @param player2 jugador que se une
     * @return CompletableFuture con la partida actualizada
     */
    CompletableFuture<Game> joinMultiplayerGame(String gameId, Player player2);

    /**
     * Inicia la partida cuando ambos jugadores están listos.
     *
     * @param gameId ID de la partida
     * @return CompletableFuture completado cuando la partida inicia
     */
    CompletableFuture<Void> startMultiplayerGame(String gameId);

    /**
     * Envía un movimiento al servidor (Firebase) para sincronización multijugador.
     *
     * @param gameId ID de la partida
     * @param move información del movimiento
     * @return CompletableFuture que se completa cuando el servidor confirma
     */
    CompletableFuture<Void> sendRemoteMove(String gameId, RemoteMoveDto move);

    /**
     * Se suscribe a cambios de movimientos remotos en una partida.
     *
     * @param gameId ID de la partida
     * @param listener callback que recibe los movimientos actualizados
     * @return ID de la suscripción (para desuscribirse después)
     */
    String subscribeToRemoteMoves(String gameId, Consumer<List<RemoteMoveDto>> listener);

    /**
     * Se suscribe a cambios en el estado de la partida (turno actual, estado, etc).
     *
     * @param gameId ID de la partida
     * @param listener callback que recibe el estado actualizado
     * @return ID de la suscripción
     */
    String subscribeToGameState(String gameId, Consumer<Game> listener);

    /**
     * Obtiene el estado actual de una partida multijugador.
     *
     * @param gameId ID de la partida
     * @return CompletableFuture con el estado actual del juego
     */
    CompletableFuture<Game> getGameState(String gameId);

    /**
     * Desuscribe un listener de cambios remotos.
     *
     * @param gameId ID de la partida
     * @param listenerId ID de la suscripción devuelto por subscribe
     */
    void unsubscribeFromRemoteMoves(String gameId, String listenerId);

    /**
     * Desuscribe un listener de cambios de estado.
     *
     * @param gameId ID de la partida
     * @param listenerId ID de la suscripción devuelto por subscribe
     */
    void unsubscribeFromGameState(String gameId, String listenerId);

    /**
     * Finaliza la partida multijugador.
     *
     * @param gameId ID de la partida
     * @param winnerId ID del jugador ganador
     * @return CompletableFuture completado cuando la partida se finaliza
     */
    CompletableFuture<Void> finishMultiplayerGame(String gameId, String winnerId);

    /**
     * Abandona la partida actual.
     *
     * @param gameId ID de la partida
     * @return CompletableFuture completado cuando se procesa el abandono
     */
    CompletableFuture<Void> abandonMultiplayerGame(String gameId);

    /**
     * Obtiene lista de partidas disponibles (esperando jugador) en Firebase.
     *
     * @return CompletableFuture con lista de IDs de partidas disponibles
     */
    CompletableFuture<List<String>> getAvailableGames();
}


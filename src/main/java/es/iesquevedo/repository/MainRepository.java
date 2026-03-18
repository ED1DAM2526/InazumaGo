package es.iesquevedo.repository;

import es.iesquevedo.dto.GameDto;

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
     * Ejemplo de payload:
     * {
     *   "moves": [
     *     { "playerId": "player1", "move": "KICK", "position": {"x": 10, "y": 20} },
     *     { "playerId": "player2", "move": "PASS", "position": {"x": 15, "y": 25} }
     *   ],
     *   "timestamp": 1710786000000
     * }
     *
     * @param gameId ID de la partida
     * @param payload objeto con la estructura de movimientos
     * @return CompletableFuture que resuelve cuando se confirma la escritura
     */
    CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload);

    /**
     * Suscribe un listener a cambios en los movimientos de una partida.
     * Se llamará cada vez que haya nuevos movimientos.
     *
     * @param gameId ID de la partida
     * @param listener función que recibe el array de movimientos actualizado
     * @return ID de la suscripción (para poder desuscribirse después)
     */
    String addMovesListener(String gameId, Consumer<java.util.List<MoveData>> listener);

    /**
     * Desuscribe un listener por su ID.
     * @param gameId ID de la partida
     * @param listenerId ID retornado por addMovesListener()
     */
    void removeMovesListener(String gameId, String listenerId);

    String findDefaultName();
}

package es.iesquevedo.repository;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Contrato de acceso a datos para partidas y movimientos.
 *
 * <p>Incluye operaciones CRUD minimas para {@link GameDto} y operaciones especificas
 * para publicar/escuchar movimientos en tiempo real.</p>
 *
 * <p>Reglas generales de contrato:</p>
 * <ul>
 *   <li>Las operaciones asincronas no deben bloquear el hilo llamador.</li>
 *   <li>Si el recurso no existe, la operacion debe completar con {@code null}
 *       o con excepcion de dominio segun el caso documentado.</li>
 *   <li>Las implementaciones pueden devolver copias defensivas para evitar mutaciones externas.</li>
 * </ul>
 */
public interface MainRepository {

    /**
     * Crea una partida nueva.
     *
     * @param game partida a persistir. Debe incluir ID unico no vacio.
     * @return la partida creada (o su representacion persistida).
     */
    default CompletableFuture<GameDto> createGame(GameDto game) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("createGame not implemented")
        );
    }

    /**
     * Lista partidas disponibles.
     *
     * @return listado de partidas. Si no hay datos, devuelve lista vacia.
     */
    default CompletableFuture<List<GameDto>> listGames() {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("listGames not implemented")
        );
    }

    /**
     * Lee una partida completa por su ID (operacion R de CRUD).
     *
     * @param gameId ID unico de la partida.
     * @return partida encontrada o {@code null} si no existe.
     */
    CompletableFuture<GameDto> getGame(String gameId);

    /**
     * Actualiza una partida existente (operacion U de CRUD).
     *
     * @param gameId ID de la partida a actualizar.
     * @param game nuevos datos de la partida.
     * @return la partida actualizada o {@code null} si no existe.
     */
    default CompletableFuture<GameDto> updateGame(String gameId, GameDto game) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("updateGame not implemented")
        );
    }

    /**
     * Elimina una partida por su ID (operacion D de CRUD).
     *
     * @param gameId ID de la partida a eliminar.
     * @return operacion completada cuando la eliminacion termina.
     */
    default CompletableFuture<Void> deleteGame(String gameId) {
        return CompletableFuture.failedFuture(
                new UnsupportedOperationException("deleteGame not implemented")
        );
    }

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
     * @param gameId ID de la partida.
     * @param payload objeto con la estructura de movimientos.
     * @return operacion completada cuando se confirma la escritura.
     */
    CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload);

    /**
     * Suscribe un listener a cambios en los movimientos de una partida.
     * Se llamará cada vez que haya nuevos movimientos.
     *
     * @param gameId ID de la partida.
     * @param listener funcion que recibe el array de movimientos actualizado.
     * @return ID de la suscripcion (para poder desuscribirse despues).
     */
    String addMovesListener(String gameId, Consumer<java.util.List<MoveData>> listener);

    /**
     * Desuscribe un listener por su ID.
     * @param gameId ID de la partida.
     * @param listenerId ID retornado por addMovesListener().
     */
    void removeMovesListener(String gameId, String listenerId);

    /**
     * Recupera el nombre por defecto usado por el saludo principal.
     *
     * <p>Metodo temporal para mantener compatibilidad con E1-US2.</p>
     *
     * @return nombre por defecto o {@code null} si no existe.
     */
    String findDefaultName();
}

package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Interfaz para el repositorio de partidas basado en Firebase Realtime Database.
 * Define contratos para obtener partidas, escribir movimientos en múltiples paths y escuchar cambios en movimientos.
 */
public interface FirebaseGameRepository {

    /**
     * Obtiene una partida completa por su ID desde Firebase.
     *
     * Ejemplo de respuesta JSON:
     * {
     *   "id": "game123",
     *   "name": "Partida de Prueba",
     *   "players": ["player1", "player2"],
     *   "status": "IN_PROGRESS",
     *   "createdAt": 1710786000000,
     *   "moves": [
     *     {
     *       "playerId": "player1",
     *       "move": "KICK",
     *       "position": {"x": 10.0, "y": 20.0},
     *       "timestamp": 1710786001000
     *     }
     *   ]
     * }
     *
     * @param gameId ID único de la partida
     * @return CompletableFuture que resuelve a un GameDto o null si no existe
     */
    CompletableFuture<GameDto> getGame(String gameId);

    /**
     * Escribe movimientos en múltiples paths de una partida usando una petición PATCH atómica.
     *
     * Ejemplo de payload JSON enviado:
     * {
     *   "moves": [
     *     {
     *       "playerId": "player1",
     *       "move": "KICK",
     *       "position": {"x": 10.0, "y": 20.0},
     *       "timestamp": 1710786001000
     *     },
     *     {
     *       "playerId": "player2",
     *       "move": "PASS",
     *       "position": {"x": 15.0, "y": 25.0},
     *       "timestamp": 1710786002000
     *     }
     *   ],
     *   "timestamp": 1710786000000,
     *   "gameVersion": "1710786000000"
     * }
     *
     * @param gameId ID de la partida
     * @param payload objeto con la estructura de movimientos
     * @return CompletableFuture que resuelve cuando se confirma la escritura
     */
    CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload);

    /**
     * Suscribe un listener a cambios en los movimientos de una partida para actualizaciones en tiempo real.
     *
     * Ejemplo de evento JSON recibido en el listener:
     * [
     *   {
     *     "playerId": "player1",
     *     "move": "KICK",
     *     "position": {"x": 10.0, "y": 20.0},
     *     "timestamp": 1710786001000
     *   },
     *   {
     *     "playerId": "player2",
     *     "move": "PASS",
     *     "position": {"x": 15.0, "y": 25.0},
     *     "timestamp": 1710786002000
     *   }
     * ]
     *
     * @param gameId ID de la partida
     * @param listener función que recibe la lista actualizada de movimientos
     * @return ID de la suscripción (para poder desuscribirse después)
     */
    String addMovesListener(String gameId, Consumer<List<MoveData>> listener);
}

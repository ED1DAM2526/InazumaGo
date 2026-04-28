package es.iesquevedo.repository;

import java.util.Map;

public interface MainRepository {

    /**
     * Escribe múltiples rutas de forma atómica usando PATCH multi-path de Firebase.
     * @param updates mapa de ruta → valor (ej: {"/games/123/moves/0": moveData})
     * @param idToken token JWT obtenido del AuthService
     */
    void writeMovesMultiPath(Map<String, Object> updates, String idToken);

    /**
     * Obtiene el estado de un juego.
     * @param gameId ID del juego
     * @param idToken token JWT
     * @return mapa con los datos del juego
     */
    Map<String, Object> getGame(String gameId, String idToken);

    /**
     * Registra un listener para cambios en tiempo real (streaming SSE).
     * Para tests, esta interfaz permite simular listeners.
     * @param path ruta a escuchar
     * @param listener callback al recibir cambios
     */
    void addMovesListener(String path, MovesListener listener);

    /** Interfaz funcional para el listener */
    interface MovesListener {
        void onMovesUpdated(Map<String, Object> data);
    }
}
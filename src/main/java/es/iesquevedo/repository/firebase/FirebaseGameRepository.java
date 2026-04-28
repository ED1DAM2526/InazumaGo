package es.iesquevedo.repository.firebase;


/**
 * Interfaz para el repositorio de partidas basado en Firebase Realtime Database.
 * Define contratos para obtener partidas, escribir movimientos en múltiples paths y escuchar cambios en movimientos.
 */


 import es.iesquevedo.repository.MainRepository;

 import java.util.*;

    /**
     * Stub para tests y desarrollo local. NO usar en producción.
     * Simula respuestas configurables de Firebase.
     */
    class FirebaseMainRepositoryStub implements MainRepository {

        private final String endpoint;
        private final int timeout;
        private boolean shouldReject = false;
        private Map<String, Object> fakeGameData = new HashMap<>();

        public FirebaseMainRepositoryStub(String endpoint, int timeout) {
            this.endpoint = endpoint;
            this.timeout = timeout;
        }

        /** Configura el stub para simular rechazo (403) */
        public void setShouldReject(boolean reject) {
            this.shouldReject = reject;
        }

        /** Configura datos falsos que devolverá getGame */
        public void setFakeGameData(Map<String, Object> data) {
            this.fakeGameData = data;
        }

        @Override
        public void writeMovesMultiPath(Map<String, Object> updates, String idToken) {
            if (shouldReject) {
                throw new RuntimeException("Firebase rejected: 403 Forbidden");
            }
            System.out.println("[STUB] writeMovesMultiPath: " + updates);
        }

        @Override
        public Map<String, Object> getGame(String gameId, String idToken) {
            if (shouldReject) {
                throw new RuntimeException("Firebase rejected: 403 Forbidden");
            }
            return fakeGameData;
        }

        @Override
        public void addMovesListener(String path, MovesListener listener) {
            System.out.println("[STUB] addMovesListener en: " + path);
            // En tests se puede llamar manualmente: listener.onMovesUpdated(data)
        }
    }

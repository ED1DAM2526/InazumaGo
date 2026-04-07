package es.iesquevedo.repository.firebase;

import es.iesquevedo.repository.MainRepository;
import java.io.IOException;
import java.util.Map;

/**
 * Implementación de producción del repositorio basada en Firebase Realtime Database (REST/streaming).
 */
public class FirebaseMainRepository implements MainRepository {
    private final String firebaseUrl;

    public FirebaseMainRepository(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    @Override
    public String findDefaultName() {
        return "FirebasePlayer";
    }

    /**
     * Realiza una actualización multi-path con PATCH en Firebase.
     */
    public boolean patchMultiPath(String path, Map<String, Object> updates) throws IOException {
        // TODO: Implementar llamada HTTP real con OkHttp
        // Por ahora, simulamos éxito
        return true;
    }
}
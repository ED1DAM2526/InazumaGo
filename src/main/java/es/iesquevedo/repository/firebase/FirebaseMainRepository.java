package es.iesquevedo.repository.firebase;

import es.iesquevedo.repository.MainRepository;

/**
 * Implementación de producción del repositorio basada en Firebase Realtime Database (REST/streaming).
 * Este es un esqueleto: implementar HTTP client, autenticación (idToken) y parsing/serialización.
 */
public class FirebaseMainRepository implements MainRepository {
    private final String firebaseUrl; // base URL de la RTDB (por ejemplo https://<PROJECT>.firebaseio.com)

    public FirebaseMainRepository(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    @Override
    public String findDefaultName() {
        // TODO: Implementar llamada REST a Firebase Realtime DB y parseo de la respuesta.
        // Este placeholder permite compilar y debe sustituirse por la lógica real.
        return "FirebasePlayer";
    }
}


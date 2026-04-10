package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.repository.MainRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FirebaseMainRepository implements MainRepository {
    private final String firebaseUrl;

    public FirebaseMainRepository(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return "firebase-listener-" + System.currentTimeMillis();
    }

    @Override
    public void removeMovesListener(String gameId, String listenerId) {
    }

    @Override
    public String findDefaultName() {
        return "FirebasePlayer";
    }

    // Método adicional para pruebas con WireMock (no está en la interfaz)
    public boolean patchMultiPath(String path, Map<String, Object> updates) throws IOException {
        // TODO: Implementar llamada HTTP real con OkHttp
        return true;
    }
}
package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Implementación de producción del repositorio basada en Firebase Realtime Database (REST/streaming).
 * Este es un esqueleto: implementar HTTP client, autenticación (idToken) y parsing/serialización.
 */
public class FirebaseMainRepository implements FirebaseGameRepository {
    private final String firebaseUrl; // base URL de la RTDB (por ejemplo https://<PROJECT>.firebaseio.com)

    public FirebaseMainRepository(String firebaseUrl) {
        this.firebaseUrl = firebaseUrl;
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        // TODO: Implementar llamada REST GET a Firebase Realtime DB para obtener la partida.
        // Usar firebaseUrl + "/games/" + gameId + ".json"
        // Parsear la respuesta JSON a GameDto.
        // Este placeholder permite compilar y debe sustituirse por la lógica real.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        // TODO: Implementar llamada REST PATCH a Firebase Realtime DB para escribir movimientos en múltiples paths.
        // Usar firebaseUrl + "/games/" + gameId + ".json" con payload serializado.
        // Confirmar escritura y resolver el CompletableFuture.
        // Este placeholder permite compilar y debe sustituirse por la lógica real.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        // TODO: Implementar suscripción a cambios en tiempo real usando Firebase Streaming API.
        // Usar firebaseUrl + "/games/" + gameId + "/moves.json" para escuchar eventos.
        // Registrar el listener y retornar un ID único para desuscripción.
        // Este placeholder permite compilar y debe sustituirse por la lógica real.
        return "listenerId-" + gameId;
    }

    @Override
    public String findDefaultName() {
        // TODO: Obtener el nombre por defecto desde Firebase o configuración.
        // Este placeholder permite compilar y debe sustituirse por la lógica real.
        return "DefaultPlayer";
    }
}

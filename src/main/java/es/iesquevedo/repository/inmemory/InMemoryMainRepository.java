package es.iesquevedo.repository.inmemory;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.repository.MainRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class InMemoryMainRepository implements MainRepository {

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        // Usar el constructor con 5 parámetros
        GameDto game = new GameDto(gameId, "InMemory", List.of(), "active", System.currentTimeMillis());
        return CompletableFuture.completedFuture(game);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MovePayload payload) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return "mock-listener-id";
    }

    @Override
    public void removeMovesListener(String gameId, String listenerId) {
    }

    @Override
    public String findDefaultName() {
        return "InazumaGoPrevio";
    }
}
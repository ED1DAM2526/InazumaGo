package es.iesquevedo.service;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MainService {
    String greet();

    CompletableFuture<GameDto> getGame(String gameId);

    CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload);

    String addMovesListener(String gameId, Consumer<List<MoveData>> listener);
}


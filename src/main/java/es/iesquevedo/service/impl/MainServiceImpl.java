package es.iesquevedo.service.impl;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.MainService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MainServiceImpl implements MainService {
    private final MainRepository repository;

    public MainServiceImpl(MainRepository repository) {
        this.repository = repository;
    }

    @Override
    public String greet() {
        String name = repository.findDefaultName();
        if (name == null || name.isBlank()) {
            throw new NotFoundException("Default player name not found");
        }
        return name.endsWith("!") ? "Hello, " + name : "Hello, " + name + "!";
    }

    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return repository.getGame(gameId);
    }

    @Override
    public CompletableFuture<Void> writeMoveMultiPath(String gameId, MoveDto payload) {
        return repository.writeMoveMultiPath(gameId, payload);
    }

    @Override
    public String addMovesListener(String gameId, Consumer<List<MoveData>> listener) {
        return repository.addMovesListener(gameId, listener);
    }
}

package es.iesquevedo.service.impl;

import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.GameService;

import java.util.concurrent.CompletableFuture;

public class GameServiceImpl implements GameService {

    private final MainRepository repository;

    public GameServiceImpl(MainRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompletableFuture<String> createGame(String playerId) {
        return repository.createGame(playerId);
    }

    @Override
    public CompletableFuture<GameStateDto> joinGame(String gameId, String playerId) {
        return repository.joinGame(gameId, playerId);
    }

    @Override
    public CompletableFuture<GameStateDto> submitMove(String gameId, String playerId, MovePayload payload) {
        String nonce = String.valueOf(System.currentTimeMillis());
        return repository.submitMove(gameId, playerId, payload, nonce);
    }

    @Override
    public CompletableFuture<GameStateDto> getGameState(String gameId) {
        return repository.getGameState(gameId);
    }

    @Override
    public CompletableFuture<Void> resign(String gameId, String playerId) {
        // For now, just return completed future
        // Later: mark game as finished with winner = other player
        return CompletableFuture.completedFuture(null);
    }
}
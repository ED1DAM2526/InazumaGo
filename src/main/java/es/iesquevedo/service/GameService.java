package es.iesquevedo.service;

import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MovePayload;

import java.util.concurrent.CompletableFuture;

public interface GameService {
    CompletableFuture<String> createGame(String playerId);
    CompletableFuture<GameStateDto> joinGame(String gameId, String playerId);
    CompletableFuture<GameStateDto> submitMove(String gameId, String playerId, MovePayload payload);
    CompletableFuture<GameStateDto> getGameState(String gameId);
    CompletableFuture<Void> resign(String gameId, String playerId);
}
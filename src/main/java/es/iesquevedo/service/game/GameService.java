package es.iesquevedo.service.game;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.game.ScoreSnapshot;
import es.iesquevedo.model.player.Player;

import java.util.concurrent.CompletableFuture;

/**
 * Interfaz de servicio de juego
 */
public interface GameService {
    
    /**
     * Crear nueva partida online
     */
    CompletableFuture<String> createOnlineGame(Player hostPlayer);
    
    /**
     * Unirse a partida existente
     */
    CompletableFuture<Void> joinOnlineGame(String gameId, Player joiningPlayer);
    
    /**
     * Realizar movimiento
     */
    CompletableFuture<Void> makeMove(String gameId, String playerId, 
                                     Position position, String clientNonce);
    
    /**
     * Realizar pase
     */
    CompletableFuture<Void> makePass(String gameId, String playerId, String clientNonce);
    
    /**
     * Obtener partida
     */
    CompletableFuture<GameDto> getGame(String gameId);
    
    /**
     * Obtener puntuación provisional
     */
    CompletableFuture<ScoreSnapshot> getProvisionalScore(String gameId);
}


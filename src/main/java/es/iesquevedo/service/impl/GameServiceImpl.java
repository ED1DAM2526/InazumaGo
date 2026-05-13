package es.iesquevedo.service.impl;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.GameService;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de GameService.
 * Gestiona la lógica de turnos y validaciones en memoria.
 */
public class GameServiceImpl implements GameService {

    // Almacenamiento en memoria (será reemplazado por repositorio en producción)
    private final Map<String, Game> games = new HashMap<>();

    @Override
    public Game createGame(String gameName, Player player1) {
        if (gameName == null || gameName.isEmpty()) {
            throw new IllegalArgumentException("Nombre de partida no puede estar vacío");
        }
        if (player1 == null) {
            throw new IllegalArgumentException("Jugador 1 no puede ser nulo");
        }
        
        Game game = new Game(gameName, player1);
        games.put(game.getId(), game);
        return game;
    }

    @Override
    public Game joinGame(String gameId, Player player2) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }
        if (game.getState() != GameState.WAITING) {
            throw new IllegalStateException("La partida no está en estado WAITING");
        }
        
        game.addPlayer(player2);
        return game;
    }

    @Override
    public Game startGame(String gameId) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }
        
        game.start();
        return game;
    }

    @Override
    public Game executeMove(String gameId, String playerId, Object moveData) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }

        // Validar que es turno del jugador
        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null || !currentPlayer.getId().equals(playerId)) {
            throw new PlayerNotInTurnException("No es el turno del jugador: " + playerId);
        }

        // Validar que el jugador está vivo
        if (!currentPlayer.isAlive()) {
            throw new InvalidMoveException("El jugador no está vivo");
        }

        // Validar que la partida está en curso
        if (game.getState() != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("La partida no está en curso");
        }

        // Aquí iría validación específica de reglas del juego
        // Por ahora, aceptamos el movimiento passively
        // (MOTOR completa con lógica real según reglamento)

        return game;
    }

    @Override
    public Game nextTurn(String gameId) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }

        if (game.getState() != GameState.IN_PROGRESS) {
            throw new IllegalStateException("La partida no está en curso");
        }

        game.nextTurn();
        return game;
    }

    @Override
    public Game finishGame(String gameId, String winnerPlayerId) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }

        game.finish(winnerPlayerId);
        return game;
    }

    @Override
    public Game abandonGame(String gameId) {
        Game game = getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Partida no encontrada: " + gameId);
        }

        game.abandon();
        return game;
    }

    @Override
    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    /**
     * Método auxiliar para tests: resetea el almacenamiento en memoria.
     */
    public void reset() {
        games.clear();
    }
}

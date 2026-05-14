package es.iesquevedo.service.impl;

import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.MoveValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación de GameService.
 * Gestiona la lógica de turnos y validaciones en memoria.
 */
public class GameServiceImpl implements GameService {

    // Almacenamiento en memoria (será reemplazado por repositorio en producción)
    private final Map<String, Game> games = new HashMap<>();
    private final MoveValidator moveValidator;

    public GameServiceImpl() {
        this.moveValidator = new InazumaGoMoveValidator();
    }

    public GameServiceImpl(MoveValidator moveValidator) {
        this.moveValidator = moveValidator;
    }

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

        if (!(moveData instanceof Move)) {
            throw new IllegalArgumentException("moveData debe ser instancia de Move");
        }

        Move move = (Move) moveData;

        // Validar movimiento con reglas de Inazuma Go
        moveValidator.validateMove(game, move);

        // Guardar estado previo del tablero para detectar repetición
        Board previousBoardState = game.getBoard().clone();

        if (move.isPass()) {
            // Registrar pase
            game.getMoves().add(move);
            game.incrementConsecutivePasses();

            // Doble pase = fin de partida
            if (game.getConsecutivePasses() >= 2) {
                game.setState(GameState.FINISHED);
                // Determinar ganador por puntuación (simplificado)
                determineWinner(game);
                return game;
            }
        } else {
            // Ejecutar movimiento: colocar piedra
            int color = game.getCurrentPlayerIndex() == 0 ? 1 : 2; // 1=negro, 2=blanco
            Board board = game.getBoard();
            board.placeStone(move.getRow(), move.getCol(), color);

            // Capturar grupos enemigos sin libertades
            int capturedCount = board.captureGroupsWithoutLiberties();
            move.setCapturedCount(capturedCount);

            // Registrar movimiento
            game.getMoves().add(move);

            // Reiniciar contador de pases si hubo captura
            if (capturedCount > 0) {
                game.resetConsecutivePasses();
            } else {
                game.resetConsecutivePasses(); // También reinicia con colocación normal
            }

            // Detectar repetición: si el tablero es igual al estado previo, fin de partida
            if (game.getLastBoardState() != null && game.getLastBoardState().equals(board)) {
                game.setState(GameState.FINISHED);
                determineWinner(game);
                return game;
            }
        }

        // Guardar estado actual como "último estado" para próxima comparación
        game.setLastBoardState(previousBoardState);

        // Cambiar turno
        game.nextTurn();

        return game;
    }

    /**
     * Determina el ganador de acuerdo a la puntuación simplificada.
     * (En versión completa, implementar algoritmo de conteo según reglamento)
     */
    private void determineWinner(Game game) {
        // Versión simplificada: cuenta piedras en el tablero
        // TODO: Implementar conteo completo según reglamento (libertades, territorio, komi, etc.)
        int blackStones = 0;
        int whiteStones = 0;

        Board board = game.getBoard();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getCell(r, c) == 1) blackStones++;
                else if (board.getCell(r, c) == 2) whiteStones++;
            }
        }

        // Komi de 5.5 para Blanco
        double blackScore = blackStones;
        double whiteScore = whiteStones + 5.5;

        if (blackScore > whiteScore) {
            game.setWinnerPlayerId(game.getPlayers().get(0).getId());
        } else {
            game.setWinnerPlayerId(game.getPlayers().get(1).getId());
        }
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

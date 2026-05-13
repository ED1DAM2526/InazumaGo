package es.iesquevedo.service.game;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveDto;
import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.OutOfTurnException;
import es.iesquevedo.model.board.Position;
import es.iesquevedo.model.game.Game;
import es.iesquevedo.model.game.GameState;
import es.iesquevedo.model.move.Move;
import es.iesquevedo.model.move.MoveExecutor;
import es.iesquevedo.model.move.MoveResult;
import es.iesquevedo.model.move.MoveValidator;
import es.iesquevedo.model.player.Player;
import es.iesquevedo.model.player.PlayerColor;
import es.iesquevedo.model.scoring.ChineseScorerImpl;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.dto.mapper.GameMapper;
import es.iesquevedo.dto.mapper.MoveMapper;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Servicio de juego: orquesta lógica del motor con persistencia en Firebase
 */
public class GameServiceImpl implements GameService {
    private static final Logger logger = Logger.getLogger(GameServiceImpl.class.getName());

    private final MainRepository repository;
    private final MoveValidator validator;
    private final MoveExecutor executor;
    private final ChineseScorerImpl scorer;

    private static final int MAX_UPDATE_RETRIES = 3;

    public GameServiceImpl(MainRepository repository) {
        this.repository = repository;
        this.validator = new MoveValidator();
        this.executor = new MoveExecutor();
        this.scorer = new ChineseScorerImpl();
    }

    /**
     * Crear una nueva partida online
     */
    @Override
    public CompletableFuture<String> createOnlineGame(Player hostPlayer) {
        return CompletableFuture.supplyAsync(() -> {
            Game game = new Game(java.util.UUID.randomUUID().toString());
            game.addPlayer(new Player(hostPlayer.getId(), PlayerColor.BLACK, hostPlayer.getDisplayName()));
            
            GameDto dto = GameMapper.toDto(game);
            logger.info("Creating game: " + game.getGameId());
            
            return new Object[] { game.getGameId(), dto };
        }).thenCompose(obj -> {
            Object[] pair = (Object[]) obj;
            String gameId = (String) pair[0];
            GameDto dto = (GameDto) pair[1];
            // Guardar en repositorio
            return repository.updateGame(gameId, dto).thenApply(v -> gameId);
        });
    }

    /**
     * Unirse a una partida existente
     */
    @Override
    public CompletableFuture<Void> joinOnlineGame(String gameId, Player joiningPlayer) {
        return repository.getGame(gameId)
            .thenCompose(gameDto -> {
                Game game = GameMapper.toEntity(gameDto);
                
                if (game.getState() != GameState.WAITING) {
                    return CompletableFuture.failedFuture(
                        new InvalidMoveException("Game is not waiting for players")
                    );
                }
                
                game.addPlayer(new Player(joiningPlayer.getId(), PlayerColor.WHITE, joiningPlayer.getDisplayName()));
                game.startGame();
                
                GameDto updated = GameMapper.toDto(game);
                logger.info("Player joined game: " + gameId);
                
                return updateWithRetry(gameId, updated);
            });
    }

    /**
     * Ejecutar un movimiento con validación completa
     */
    @Override
    public CompletableFuture<Void> makeMove(String gameId, String playerId, 
                                            Position position, String clientNonce) {
        return repository.getGame(gameId)
            .thenCompose(gameDto -> {
                Game game = GameMapper.toEntity(gameDto);
                
                if (game.getState() != GameState.PLAYING) {
                    return CompletableFuture.failedFuture(
                        new InvalidMoveException("Game is not playing")
                    );
                }

                // Verificar que playerId coincide con el jugador que tiene el turno
                PlayerColor expectedColor = game.getCurrentPlayer();
                Player expectedPlayer = game.getPlayer(expectedColor);
                if (expectedPlayer == null || !expectedPlayer.getId().equals(playerId)) {
                    return CompletableFuture.failedFuture(
                        new OutOfTurnException("Player is not the one with the current turn")
                    );
                }

                // Crear movimiento
                Move move = new Move(position, expectedColor, clientNonce);

                // VALIDAR EN MOTOR (crítico)
                es.iesquevedo.model.move.ValidationResult validation = validator.validate(
                    move,
                    game.getBoard(),
                    game.getCurrentPlayer(),
                    game.getBoardHistory()
                );
                
                if (!validation.isValid()) {
                    logger.warning("Invalid move: " + validation.getReason());
                    return CompletableFuture.failedFuture(
                        new InvalidMoveException(validation.getReason())
                    );
                }
                
                // EJECUTAR MOVIMIENTO
                Player currentPlayer = game.getPlayer(game.getCurrentPlayer());
                Player opponent = game.getOpponent(game.getCurrentPlayer());
                
                MoveResult result = executor.executeMove(move, game.getBoard(), 
                                                        currentPlayer, opponent);
                
                // Registrar en historial
                game.recordMove(move);
                game.resetPassCount();
                game.updateNoCaptureMoves(result.hasCaptured());
                game.nextTurn();
                
                logger.info("Move executed: " + move + " -> " + result);
                
                // GUARDAR EN FIREBASE (multi-path)
                GameDto updated = GameMapper.toDto(game);
                MoveDto moveDto = MoveMapper.toDto(move);
                
                return repository.writeMoveMultiPath(gameId, moveDto)
                    .thenCompose(v -> updateWithRetry(gameId, updated));
            })
            .exceptionally(ex -> {
                logger.severe("Error executing move: " + ex.getMessage());
                throw new RuntimeException("Move execution failed", ex);
            });
    }

    /**
     * Manejar pase
     */
    @Override
    public CompletableFuture<Void> makePass(String gameId, String playerId, 
                                            String clientNonce) {
        return repository.getGame(gameId)
            .thenCompose(gameDto -> {
                Game game = GameMapper.toEntity(gameDto);
                
                if (game.getState() != GameState.PLAYING) {
                    return CompletableFuture.failedFuture(
                        new InvalidMoveException("Game is not playing")
                    );
                }

                // Verificar que playerId coincide con el jugador que tiene el turno
                PlayerColor expectedColor = game.getCurrentPlayer();
                Player expectedPlayer = game.getPlayer(expectedColor);
                if (expectedPlayer == null || !expectedPlayer.getId().equals(playerId)) {
                    return CompletableFuture.failedFuture(
                        new OutOfTurnException("Player is not the one with the current turn")
                    );
                }

                // Crear movimiento de pase
                Move pass = Move.pass(game.getCurrentPlayer(), clientNonce);
                game.recordMove(pass);
                game.handlePass();
                
                // Si se alcanzó doble pase, finalizar
                if (game.getState() == GameState.FINISHED) {
                    var scorer = new ChineseScorerImpl();
                    var result = scorer.calculateFinalScore(
                        game.getBoard(),
                        game.getPlayer(PlayerColor.BLACK),
                        game.getPlayer(PlayerColor.WHITE),
                        "Double Pass"
                    );
                    game.setResult(result);
                    logger.info("Game finished by double pass: " + result);
                } else {
                    game.nextTurn();
                }
                
                GameDto updated = GameMapper.toDto(game);
                return updateWithRetry(gameId, updated);
            });
    }

    /**
     * Obtener partida
     */
    @Override
    public CompletableFuture<GameDto> getGame(String gameId) {
        return repository.getGame(gameId);
    }

    /**
     * Obtener puntuación provisional
     */
    @Override
    public CompletableFuture<es.iesquevedo.model.game.ScoreSnapshot> getProvisionalScore(String gameId) {
        return repository.getGame(gameId)
            .thenApply(gameDto -> {
                Game game = GameMapper.toEntity(gameDto);
                return scorer.calculateProvisionalScore(
                    game.getBoard(),
                    game.getPlayer(PlayerColor.BLACK),
                    game.getPlayer(PlayerColor.WHITE)
                );
            });
    }

    private CompletableFuture<Void> updateWithRetry(String gameId, GameDto updated) {
        updated.setGameVersion(String.valueOf(System.currentTimeMillis()));
        return repository.updateGame(gameId, updated).handle((v, ex) -> {
            if (ex == null) return null;
            // Intentar reintento simple
            for (int i = 1; i <= MAX_UPDATE_RETRIES; i++) {
                try {
                    Thread.sleep(50 * i);
                    GameDto latest = repository.getGame(gameId).join();
                    // merge minimal: overwrite game fields
                    latest.setName(updated.getName());
                    latest.setPlayers(updated.getPlayers());
                    latest.setStatus(updated.getStatus());
                    latest.setMoves(updated.getMoves());
                    latest.setGameVersion(String.valueOf(System.currentTimeMillis()));
                    repository.updateGame(gameId, latest).join();
                    return null;
                } catch (Exception retryEx) {
                    // continue retry
                }
            }
            throw new RuntimeException("Failed to update game after retries", ex);
        });
    }
}

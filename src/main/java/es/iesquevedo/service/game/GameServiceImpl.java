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
            
            return game.getGameId();
        }).thenCompose(gameId -> 
            // Guardar en Firebase
            repository.getGame(gameId)
                .thenApply(g -> gameId)
                .exceptionally(e -> gameId)
        );
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
                
                return repository.updateGame(gameId, updated).thenApply(v -> (Void) null);
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
                
                // Crear movimiento
                Move move = new Move(position, game.getCurrentPlayer(), clientNonce);
                
                // VALIDAR EN MOTOR (crítico)
                MoveValidator.ValidationResult validation = validator.validate(
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
                    .thenCompose(v -> repository.updateGame(gameId, updated));
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
                return repository.updateGame(gameId, updated).thenApply(v -> (Void) null);
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
}

// Imports needed
import es.iesquevedo.dto.mapper.GameMapper;
import es.iesquevedo.dto.mapper.MoveMapper;


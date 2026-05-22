package es.iesquevedo.controller;

import es.iesquevedo.config.AppState;
import es.iesquevedo.dto.RemoteMoveDto;
import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import es.iesquevedo.service.impl.MultiplayerGameServiceImpl;
import es.iesquevedo.service.MultiplayerGameService;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para partidas multijugador sincronizadas con Firebase.
 * Extiende la funcionalidad de GameController para soportar múltiples dispositivos.
 */
public class MultiplayerGameController {
    private static final Logger LOGGER = Logger.getLogger(MultiplayerGameController.class.getName());
    private static final int BOARD_SIZE = 9;
    private static final int CELL_SIZE = 50;

    @FXML private Label player1NameLabel;
    @FXML private Label player1ScoreLabel;
    @FXML private Label player1TimeLabel;
    @FXML private Label player2NameLabel;
    @FXML private Label player2ScoreLabel;
    @FXML private Label player2TimeLabel;
    @FXML private Label currentTurnLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Canvas boardCanvas;

    private String gameId;
    private String currentPlayerId;
    private String localPlayerName;
    private String remotePlayerName;

    private Game game;
    private MultiplayerGameService multiplayerService;
    private InazumaGoMoveValidator moveValidator;
    private FirebaseMainRepository repository;

    private long player1TimeMs = 0;
    private long player2TimeMs = 0;
    private AnimationTimer gameTimer;
    private long lastTime = 0;
    private boolean gameEnded = false;
    private boolean moveInProgress = false;

    private String movesListenerId;
    private String gameStateListenerId;

    @FXML
    public void initialize() {
        LOGGER.log(Level.INFO, "MultiplayerGameController inicializado");
        moveValidator = new InazumaGoMoveValidator();
        boardCanvas.setOnMouseClicked(this::onBoardClick);
    }

    /**
     * Inicializa la partida multijugador.
     *
     * @param gameId ID de la partida
     * @param currentPlayerId ID del jugador actual
     * @param firebaseUrl URL de Firebase
     */
    public void initMultiplayerGame(String gameId, String currentPlayerId, String firebaseUrl) {
        this.gameId = gameId;
        this.currentPlayerId = currentPlayerId;
        this.repository = new FirebaseMainRepository(firebaseUrl);
        
        // Establecer token de Firebase si está disponible
        String token = AppState.getInstance().getAuthToken();
        if (token != null) {
            repository.setIdToken(token);
        }

        this.multiplayerService = new MultiplayerGameServiceImpl(repository);

        // Cargar estado de partida
        multiplayerService.getGameState(gameId).thenAccept(loadedGame -> {
            Platform.runLater(() -> {
                this.game = loadedGame;
                if (game != null && game.getPlayers().size() >= 2) {
                    setupPlayerInfo();
                    subscribeToRemoteUpdates();
                    drawBoard();
                    startGameTimer();
                }
            });
        }).exceptionally(ex -> {
            LOGGER.log(Level.SEVERE, "Error al cargar partida", ex);
            statusLabel.setText("Error al cargar la partida");
            return null;
        });
    }

    /**
     * Se une a una partida existente como segundo jugador.
     *
     * @param gameId ID de la partida
     * @param player jugador que se une
     * @param firebaseUrl URL de Firebase
     */
    public void joinMultiplayerGame(String gameId, Player player, String firebaseUrl) {
        this.gameId = gameId;
        this.currentPlayerId = player.getId();
        this.localPlayerName = player.getName();
        this.repository = new FirebaseMainRepository(firebaseUrl);

        String token = AppState.getInstance().getAuthToken();
        if (token != null) {
            repository.setIdToken(token);
        }

        this.multiplayerService = new MultiplayerGameServiceImpl(repository);

        // Unirse a la partida
        multiplayerService.joinMultiplayerGame(gameId, player).thenAccept(joinedGame -> {
            Platform.runLater(() -> {
                this.game = joinedGame;
                setupPlayerInfo();
                subscribeToRemoteUpdates();
                drawBoard();
                statusLabel.setText("Te has unido a la partida. Esperando que el otro jugador inicie...");
            });
        }).exceptionally(ex -> {
            LOGGER.log(Level.SEVERE, "Error al unirse a partida", ex);
            statusLabel.setText("Error al unirse: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Se suscribe a actualizaciones remotas de movimientos y estado.
     */
    private void subscribeToRemoteUpdates() {
        // Suscribirse a movimientos remotos
        movesListenerId = multiplayerService.subscribeToRemoteMoves(gameId, remoteMoves -> {
            Platform.runLater(() -> {
                for (RemoteMoveDto remoteMove : remoteMoves) {
                    if (!remoteMove.getPlayerId().equals(currentPlayerId)) {
                        applyRemoteMove(remoteMove);
                    }
                }
            });
        });

        // Suscribirse a cambios de estado del juego
        gameStateListenerId = multiplayerService.subscribeToGameState(gameId, updatedGame -> {
            Platform.runLater(() -> {
                game = updatedGame;
                updatePlayerInfo();
                drawBoard();

                if (game.getState() == GameState.IN_PROGRESS && !gameEnded) {
                    if (game.getCurrentPlayer() != null) {
                        boolean isMyTurn = game.getCurrentPlayer().getId().equals(currentPlayerId);
                        connectionStatusLabel.setText(isMyTurn ? "✓ Es tu turno" : "⏳ Turno remoto");
                    }
                }
            });
        });

        connectionStatusLabel.setText("✓ Conectado");
        LOGGER.log(Level.INFO, "Suscrito a actualizaciones multijugador");
    }

    /**
     * Aplica un movimiento remoto al tablero.
     */
    private void applyRemoteMove(RemoteMoveDto remoteMove) {
        try {
            if (remoteMove.isPass()) {
                game.nextTurn();
                game.incrementConsecutivePasses();
                statusLabel.setText(remoteMove.getPlayerName() + " pasó su turno");

                if (game.getConsecutivePasses() >= 2) {
                    endGame();
                }
            } else {
                Board board = game.getBoard();
                int playerColor = game.getCurrentPlayer() != null && 
                    game.getCurrentPlayer().getId().equals(remoteMove.getPlayerId()) ? 1 : 2;
                
                board.placeStone(remoteMove.getRow(), remoteMove.getCol(), playerColor);
                int capturedCount = board.captureGroupsWithoutLiberties();

                game.nextTurn();
                game.resetConsecutivePasses();

                statusLabel.setText(remoteMove.getPlayerName() + " colocó en [" + 
                    remoteMove.getRow() + "," + remoteMove.getCol() + "]" + 
                    (capturedCount > 0 ? " (" + capturedCount + " capturadas)" : ""));
            }

            updatePlayerInfo();
            drawBoard();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error al aplicar movimiento remoto", ex);
        }
    }

    private void setupPlayerInfo() {
        if (game == null || game.getPlayers().size() < 2) {
            return;
        }

        Player p1 = game.getPlayers().get(0);
        Player p2 = game.getPlayers().get(1);

        if (p1.getId().equals(currentPlayerId)) {
            localPlayerName = p1.getName();
            remotePlayerName = p2.getName();
        } else {
            localPlayerName = p2.getName();
            remotePlayerName = p1.getName();
        }

        updatePlayerInfo();
    }

    private void updatePlayerInfo() {
        player1NameLabel.setText(localPlayerName + " (Negro)");
        player2NameLabel.setText(remotePlayerName + " (Blanco)");
        updateScores();
        updateCurrentTurn();
    }

    private void updateScores() {
        Board board = game.getBoard();
        int blackStones = 0;
        int whiteStones = 0;

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int cell = board.getCell(r, c);
                if (cell == 1) blackStones++;
                else if (cell == 2) whiteStones++;
            }
        }

        double blackScore = blackStones;
        double whiteScore = whiteStones + 5.5;

        player1ScoreLabel.setText(String.format("Puntos: %.1f", blackScore));
        player2ScoreLabel.setText(String.format("Puntos: %.1f", whiteScore));
    }

    private void updateCurrentTurn() {
        Player currentPlayer = game.getCurrentPlayer();
        String turn = currentPlayer != null ? currentPlayer.getName() : "Desconocido";
        currentTurnLabel.setText("Turno: " + turn);
    }

    @FXML
    private void onBoardClick(MouseEvent event) {
        if (gameEnded || moveInProgress || game == null) {
            return;
        }

        // Verificar que sea turno del jugador local
        if (!game.getCurrentPlayer().getId().equals(currentPlayerId)) {
            statusLabel.setText("No es tu turno");
            return;
        }

        double x = event.getX();
        double y = event.getY();
        double boardStartX = getBoardStartX();
        double boardStartY = getBoardStartY();

        int col = (int) Math.round((x - boardStartX) / CELL_SIZE);
        int row = (int) Math.round((y - boardStartY) / CELL_SIZE);

        if (isValidPosition(row, col)) {
            placeStone(row, col);
        }
    }

    private void placeStone(int row, int col) {
        if (moveInProgress || gameEnded) {
            return;
        }

        moveInProgress = true;

        try {
            // Validar localmente primero
            Move move = new Move(currentPlayerId, row, col);
            moveValidator.validateMove(game, move);

            // Aplicar localmente
            Board board = game.getBoard();
            int playerColor = (game.getCurrentPlayerIndex() == 0) ? 1 : 2;
            board.placeStone(row, col, playerColor);
            int capturedCount = board.captureGroupsWithoutLiberties();

            game.getMoves().add(move);
            statusLabel.setText("Movimiento realizado" + 
                (capturedCount > 0 ? " (" + capturedCount + " piedras capturadas)" : ""));

            // Enviar movimiento remoto
            RemoteMoveDto remoteMove = new RemoteMoveDto(gameId, currentPlayerId, row, col);
            remoteMove.setPlayerName(localPlayerName);
            remoteMove.setTurnNumber(game.getTurnCount());

            multiplayerService.sendRemoteMove(gameId, remoteMove).thenAccept(v -> {
                Platform.runLater(() -> {
                    game.nextTurn();
                    game.resetConsecutivePasses();
                    updatePlayerInfo();
                    drawBoard();
                    moveInProgress = false;

                    LOGGER.log(Level.INFO, "Piedra colocada y sincronizada: [" + 
                        row + "," + col + "]");
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error al sincronizar movimiento: " + ex.getMessage());
                    moveInProgress = false;
                });
                return null;
            });

        } catch (InvalidMoveException | PlayerNotInTurnException ex) {
            statusLabel.setText("Movimiento inválido: " + ex.getMessage());
            moveInProgress = false;
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            moveInProgress = false;
        }
    }

    @FXML
    private void onPassTurn() {
        if (gameEnded || moveInProgress || game == null) {
            return;
        }

        if (!game.getCurrentPlayer().getId().equals(currentPlayerId)) {
            statusLabel.setText("No es tu turno");
            return;
        }

        moveInProgress = true;

        try {
            RemoteMoveDto remotePass = new RemoteMoveDto(gameId, currentPlayerId, -1, -1);
            remotePass.setPass(true);
            remotePass.setPlayerName(localPlayerName);
            remotePass.setTurnNumber(game.getTurnCount());

            multiplayerService.sendRemoteMove(gameId, remotePass).thenAccept(v -> {
                Platform.runLater(() -> {
                    game.nextTurn();
                    game.incrementConsecutivePasses();
                    statusLabel.setText(localPlayerName + " pasó su turno");

                    if (game.getConsecutivePasses() >= 2) {
                        endGame();
                    }

                    updateCurrentTurn();
                    moveInProgress = false;
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    moveInProgress = false;
                });
                return null;
            });
        } catch (Exception ex) {
            statusLabel.setText("Error al pasar turno: " + ex.getMessage());
            moveInProgress = false;
        }
    }

    @FXML
    private void onSurrender() {
        if (game == null) return;

        gameEnded = true;
        game.setState(GameState.FINISHED);

        Player winner = game.getPlayers().get((game.getCurrentPlayerIndex() + 1) % 2);
        game.setWinnerPlayerId(winner.getId());

        String message = winner.getName() + " ganó. " + localPlayerName + " se rindió";
        statusLabel.setText(message);

        multiplayerService.finishMultiplayerGame(gameId, winner.getId()).thenAccept(v -> {
            LOGGER.log(Level.INFO, message);
        });
    }

    @FXML
    private void onBackToMenu() {
        stopGameTimer();
        cleanupSubscriptions();
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/MatchingScreen.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) boardCanvas.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 500, 300));
            stage.setTitle("InazumaGo - Emparejamiento");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al volver al menú", e);
            statusLabel.setText("Error al volver");
        }
    }

    private void endGame() {
        gameEnded = true;
        game.setState(GameState.FINISHED);

        Board board = game.getBoard();
        int blackStones = 0;
        int whiteStones = 0;

        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int cell = board.getCell(r, c);
                if (cell == 1) blackStones++;
                else if (cell == 2) whiteStones++;
            }
        }

        double blackScore = blackStones;
        double whiteScore = whiteStones + 5.5;

        String winner = blackScore > whiteScore ? localPlayerName : remotePlayerName;
        String result = String.format("¡Partida finalizada! %s gana %.1f - %.1f",
            winner, Math.max(blackScore, whiteScore), Math.min(blackScore, whiteScore));

        statusLabel.setText(result);

        // Notificar al servidor
        String winnerId = blackScore > whiteScore ? 
            game.getPlayers().get(0).getId() : game.getPlayers().get(1).getId();
        
        multiplayerService.finishMultiplayerGame(gameId, winnerId).thenAccept(v -> {
            LOGGER.log(Level.INFO, result);
        });

        stopGameTimer();
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double boardStartX = getBoardStartX();
        double boardStartY = getBoardStartY();
        double boardEndX = getBoardEndX();
        double boardEndY = getBoardEndY();
        double boardSpan = (BOARD_SIZE - 1) * CELL_SIZE;

        // Fondo de madera
        LinearGradient wood = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D2A679")),
                new Stop(0.5, Color.web("#C37E3A")),
                new Stop(1, Color.web("#B06A2E"))
        );
        gc.setFill(wood);
        gc.fillRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        // Líneas del tablero
        for (int i = 0; i < BOARD_SIZE; i++) {
            double y = getIntersectionY(i);
            double x = getIntersectionX(i);

            gc.setStroke(Color.color(0.06, 0.03, 0.01, 1.0));
            gc.setLineWidth(1.2);
            gc.strokeLine(boardStartX, y, boardEndX, y);
            gc.strokeLine(x, boardStartY, x, boardEndY);
        }

        // Dibujar piedras
        if (game != null) {
            Board board = game.getBoard();
            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    int cell = board.getCell(r, c);
                    if (cell == 1) {
                        drawStone(gc, r, c, 1);
                    } else if (cell == 2) {
                        drawStone(gc, r, c, 2);
                    }
                }
            }
        }
    }

    private void drawStone(GraphicsContext gc, int row, int col, int stoneColor) {
        double x = getIntersectionX(col);
        double y = getIntersectionY(row);
        double radius = CELL_SIZE / 2 - 3;

        if (stoneColor == 1) { // Negro
            RadialGradient blackGrad = new RadialGradient(
                    45, 0.1, x - radius * 0.15, y - radius * 0.2, radius * 1.05, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#6e6e6e")),
                    new Stop(0.4, Color.web("#222222")),
                    new Stop(1.0, Color.web("#000000"))
            );
            gc.setFill(blackGrad);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        } else if (stoneColor == 2) { // Blanco
            RadialGradient whiteGrad = new RadialGradient(
                    45, 0.12, x - radius * 0.12, y - radius * 0.18, radius * 1.05, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#ffffff")),
                    new Stop(0.6, Color.web("#f2f2f2")),
                    new Stop(1.0, Color.web("#d1d1d1"))
            );
            gc.setFill(whiteGrad);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    private void startGameTimer() {
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                long elapsedNanos = now - lastTime;
                lastTime = now;

                if (!gameEnded && game != null) {
                    if (game.getCurrentPlayerIndex() == 0) {
                        player1TimeMs += elapsedNanos / 1_000_000;
                    } else {
                        player2TimeMs += elapsedNanos / 1_000_000;
                    }
                    updateTimeLabels();
                }
            }
        };
        gameTimer.start();
    }

    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    private void updateTimeLabels() {
        player1TimeLabel.setText("Tiempo: " + formatTime(player1TimeMs));
        player2TimeLabel.setText("Tiempo: " + formatTime(player2TimeMs));
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void cleanupSubscriptions() {
        if (movesListenerId != null) {
            multiplayerService.unsubscribeFromRemoteMoves(gameId, movesListenerId);
        }
        if (gameStateListenerId != null) {
            multiplayerService.unsubscribeFromGameState(gameId, gameStateListenerId);
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private double getBoardStartX() {
        return (boardCanvas.getWidth() - ((BOARD_SIZE - 1) * CELL_SIZE)) / 2.0;
    }

    private double getBoardStartY() {
        return (boardCanvas.getHeight() - ((BOARD_SIZE - 1) * CELL_SIZE)) / 2.0;
    }

    private double getBoardEndX() {
        return getBoardStartX() + ((BOARD_SIZE - 1) * CELL_SIZE);
    }

    private double getBoardEndY() {
        return getBoardStartY() + ((BOARD_SIZE - 1) * CELL_SIZE);
    }

    private double getIntersectionX(int col) {
        return getBoardStartX() + col * CELL_SIZE;
    }

    private double getIntersectionY(int row) {
        return getBoardStartY() + row * CELL_SIZE;
    }
}


package es.iesquevedo.controller;

import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalGameController {
    private static final Logger LOGGER = Logger.getLogger(LocalGameController.class.getName());
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
    @FXML private Canvas boardCanvas;
    @FXML private VBox player1Box;
    @FXML private VBox player2Box;

    private String player1Name = "Jugador Negro";
    private String player2Name = "Jugador Blanco";
    private long player1TimeMs = 0;
    private long player2TimeMs = 0;
    private AnimationTimer gameTimer;
    private long lastTime = 0;
    private boolean gameEnded = false;

    // Lógica del juego
    private Game game;
    private InazumaGoMoveValidator moveValidator;
    private boolean moveInProgress = false;

    @FXML
    public void initialize() {
        LOGGER.log(Level.INFO, "LocalGameController inicializado");
        moveValidator = new InazumaGoMoveValidator();
        boardCanvas.setOnMouseClicked(this::onBoardClick);
    }

    public void initializeLocalGame() {
        LOGGER.log(Level.INFO, "Iniciando partida local");
        
        Player player1 = new Player("1", player1Name);
        Player player2 = new Player("2", player2Name);
        
        game = new Game("Local Game", player1);
        game.addPlayer(player2);
        game.start();
        
        updatePlayerInfo();
        drawBoard();
        startGameTimer();
    }

    private void updatePlayerInfo() {
        player1NameLabel.setText(player1Name);
        player2NameLabel.setText(player2Name);
        updateScores();
        updateCurrentTurn();
    }

    private void updateCurrentTurn() {
        Player currentPlayer = game.getCurrentPlayer();
        int playerIndex = game.getCurrentPlayerIndex();
        String colorText = playerIndex == 0 ? "Negro ⚫" : "Blanco ⚪";
        currentTurnLabel.setText("📍 Turno: " + colorText);
        
        // Resaltar el jugador en turno
        if (playerIndex == 0) {
            player1Box.setStyle("-fx-border-color: #FFD700; -fx-border-width: 4; -fx-padding: 15; -fx-border-radius: 8; -fx-background-color: #FFFACD;");
            player2Box.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 3; -fx-padding: 15; -fx-background-color: #F5F5F5; -fx-border-radius: 8;");
        } else {
            player2Box.setStyle("-fx-border-color: #FFD700; -fx-border-width: 4; -fx-padding: 15; -fx-border-radius: 8; -fx-background-color: #FFFACD;");
            player1Box.setStyle("-fx-border-color: #333333; -fx-border-width: 3; -fx-padding: 15; -fx-border-radius: 8; -fx-background-color: #F0F0F0;");
        }
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

    @FXML
    private void onBoardClick(MouseEvent event) {
        if (gameEnded || moveInProgress) return;

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
        if (moveInProgress || gameEnded) return;
        
        moveInProgress = true;
        
        try {
            Player currentPlayer = game.getCurrentPlayer();
            
            if (currentPlayer == null) {
                statusLabel.setText("Error: sin jugador actual");
                moveInProgress = false;
                return;
            }
            
            Move move = new Move(currentPlayer.getId(), row, col);
            moveValidator.validateMove(game, move);
            
            Board previousBoardState = game.getBoard().clone();
            
            Board board = game.getBoard();
            int playerColor = (game.getCurrentPlayerIndex() == 0) ? 1 : 2;
            board.placeStone(row, col, playerColor);
            
            int capturedCount = board.captureGroupsWithoutLiberties();
            
            move.setCapturedCount(capturedCount);
            game.getMoves().add(move);
            
            if (game.getLastBoardState() != null && board.equals(game.getLastBoardState())) {
                statusLabel.setText("Movimiento rechazado: repetición de posición");
                moveInProgress = false;
                return;
            }
            
            game.setLastBoardState(previousBoardState);
            
            statusLabel.setText("✅ Movimiento realizado" + (capturedCount > 0 ? " (" + capturedCount + " piedras capturadas)" : ""));
            
            game.nextTurn();
            game.resetConsecutivePasses();
            
            updatePlayerInfo();
            drawBoard();
            
            moveInProgress = false;
            
            LOGGER.log(Level.INFO, "Piedra colocada en [" + row + "," + col + "]");
            
        } catch (InvalidMoveException | PlayerNotInTurnException ex) {
            statusLabel.setText("❌ Movimiento inválido: " + ex.getMessage());
            moveInProgress = false;
        }
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

        // Marco exterior
        gc.setStroke(Color.color(0.12, 0.06, 0.03, 1.0));
        gc.setLineWidth(5);
        double outerX = boardStartX - 4;
        double outerY = boardStartY - 4;
        double outerW = boardSpan + 8;
        double outerH = boardSpan + 8;
        gc.strokeRoundRect(outerX, outerY, outerW, outerH, 12, 12);

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
        Board board = game.getBoard();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int cell = board.getCell(r, c);
                if (cell == 1) drawStone(gc, r, c, 1);
                else if (cell == 2) drawStone(gc, r, c, 2);
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

    @FXML
    private void onPassTurn() {
        if (gameEnded || moveInProgress) return;
        
        moveInProgress = true;
        try {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                statusLabel.setText("Error: sin jugador actual");
                moveInProgress = false;
                return;
            }
            
            Move move = new Move(currentPlayer.getId(), true);
            
            try {
                moveValidator.validateMove(game, move);
            } catch (Exception validationEx) {
                // Continuar de todas formas - es solo un pase
                LOGGER.log(java.util.logging.Level.WARNING, "Validación de pase fallida: " + validationEx.getMessage());
            }
            
            game.getMoves().add(move);
            game.incrementConsecutivePasses();
            game.nextTurn();
            
            statusLabel.setText(currentPlayer.getName() + " pasó su turno (Pases consecutivos: " + game.getConsecutivePasses() + ")");
            
            if (game.getConsecutivePasses() >= 2) {
                endGame();
            }
            
            updateCurrentTurn();
            moveInProgress = false;
        } catch (Exception ex) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error en onPassTurn: " + ex.getMessage());
            statusLabel.setText("Error al pasar: " + ex.getMessage());
            moveInProgress = false;
            // Intentar recuperarse avanzando el turno de todas formas
            try {
                game.nextTurn();
                updateCurrentTurn();
            } catch (Exception retryEx) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error al recuperar: " + retryEx.getMessage());
            }
        }
    }

    @FXML
    private void onBackToMenu() {
        stopGameTimer();
        goBackToMainMenu();
    }

    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            Scene scene = boardCanvas.getScene();
            scene.setRoot(root);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Menú Principal");

            LOGGER.log(Level.INFO, "Volviendo al menú principal");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al menú principal: " + e.getMessage());
        }
    }

    @FXML
    private void onSurrender() {
        gameEnded = true;
        game.setState(GameState.FINISHED);
        
        Player winner = game.getPlayers().get((game.getCurrentPlayerIndex() + 1) % 2);
        Player loser = game.getCurrentPlayer();
        
        game.setWinnerPlayerId(winner.getId());
        statusLabel.setText(winner.getName() + " ganó. " + loser.getName() + " se rindió");
        
        // Mostrar diálogo de victoria
        showVictoryDialog(winner.getName(), loser.getName());
    }
    
    private void showVictoryDialog(String winnerName, String loserName) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("¡Partida Finalizada!");
        alert.setHeaderText("🏆 " + winnerName + " Ha Ganado 🏆");
        alert.setContentText(loserName + " se rindió.\n\n¿Deseas jugar otra partida?\nPulsa OK para volver al menú.");
        alert.setOnCloseRequest(e -> onBackToMenu());
        
        // Customizar el botón OK
        javafx.scene.control.ButtonType okButton = alert.getButtonTypes().get(0);
        javafx.scene.control.Button button = (javafx.scene.control.Button) alert.getDialogPane().lookupButton(okButton);
        if (button != null) {
            button.setText("Volver al Menú");
            button.setStyle("-fx-font-size: 12; -fx-padding: 8;");
        }
        
        alert.showAndWait().ifPresent(result -> {
            if (result == okButton) {
                onBackToMenu();
            }
        });
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
        
        String winner = blackScore > whiteScore ? "Negro" : "Blanco";
        String result = String.format("Partida finalizada. %s gana %.1f - %.1f", 
            winner, Math.max(blackScore, whiteScore), Math.min(blackScore, whiteScore));
        
        statusLabel.setText(result);
        stopGameTimer();
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
                if (!gameEnded) {
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


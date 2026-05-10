package es.iesquevedo.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController {
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
    private static final int BOARD_SIZE = 19;
    private static final int CELL_SIZE = 25;

    @FXML private Label player1NameLabel;
    @FXML private Label player1ScoreLabel;
    @FXML private Label player1TimeLabel;
    @FXML private Label player2NameLabel;
    @FXML private Label player2ScoreLabel;
    @FXML private Label player2TimeLabel;
    @FXML private Label currentTurnLabel;
    @FXML private Label statusLabel;
    @FXML private Canvas boardCanvas;

    private String player1Name = "Jugador 1";
    private String player2Name = "Jugador 2";
    private int player1Score = 0;
    private int player2Score = 0;
    private long player1TimeMs = 0;
    private long player2TimeMs = 0;
    private boolean isPlayer1Turn = true;
    private Stone[][] board;
    private AnimationTimer gameTimer;
    private long lastTime = 0;
    private boolean gameEnded = false;

    enum Stone {
        EMPTY, BLACK, WHITE
    }

    @FXML
    public void initialize() {
        LOGGER.log(Level.INFO, "GameController inicializado");
        board = new Stone[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = Stone.EMPTY;
            }
        }

        updatePlayerInfo();
        drawBoard();
        boardCanvas.setOnMouseClicked(this::onBoardClick);
        startGameTimer();
    }

    private void updatePlayerInfo() {
        player1NameLabel.setText(player1Name + " (Negro)");
        player2NameLabel.setText(player2Name + " (Blanco)");
        updateScores();
        updateCurrentTurn();
    }

    private void updateScores() {
        player1ScoreLabel.setText("Puntos: " + player1Score);
        player2ScoreLabel.setText("Puntos: " + player2Score);
    }

    private void updateCurrentTurn() {
        String turn = isPlayer1Turn ? player1Name : player2Name;
        currentTurnLabel.setText("Turno: " + turn);
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        for (int i = 0; i < BOARD_SIZE; i++) {
            gc.strokeLine(CELL_SIZE / 2, CELL_SIZE / 2 + i * CELL_SIZE,
                         CELL_SIZE / 2 + (BOARD_SIZE - 1) * CELL_SIZE, CELL_SIZE / 2 + i * CELL_SIZE);
            gc.strokeLine(CELL_SIZE / 2 + i * CELL_SIZE, CELL_SIZE / 2,
                         CELL_SIZE / 2 + i * CELL_SIZE, CELL_SIZE / 2 + (BOARD_SIZE - 1) * CELL_SIZE);
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != Stone.EMPTY) {
                    drawStone(gc, i, j, board[i][j]);
                }
            }
        }
    }

    private void drawStone(GraphicsContext gc, int row, int col, Stone stone) {
        double x = CELL_SIZE / 2 + col * CELL_SIZE;
        double y = CELL_SIZE / 2 + row * CELL_SIZE;
        double radius = CELL_SIZE / 2 - 2;

        if (stone == Stone.BLACK) {
            gc.setFill(Color.BLACK);
        } else {
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
        }

        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        if (stone == Stone.WHITE) {
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    @FXML
    private void onBoardClick(MouseEvent event) {
        if (gameEnded) {
            statusLabel.setText("Partida finalizada");
            return;
        }

        double x = event.getX();
        double y = event.getY();

        int col = (int) Math.round((x - CELL_SIZE / 2) / CELL_SIZE);
        int row = (int) Math.round((y - CELL_SIZE / 2) / CELL_SIZE);

        if (isValidPosition(row, col)) {
            placeStone(row, col);
        }
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void placeStone(int row, int col) {
        if (board[row][col] != Stone.EMPTY) {
            statusLabel.setText("Posición ocupada");
            return;
        }

        Stone stone = isPlayer1Turn ? Stone.BLACK : Stone.WHITE;
        board[row][col] = stone;

        if (isPlayer1Turn) {
            player1Score++;
        } else {
            player2Score++;
        }

        isPlayer1Turn = !isPlayer1Turn;
        updateScores();
        updateCurrentTurn();
        drawBoard();

        statusLabel.setText("Piedra colocada");
        LOGGER.log(Level.INFO, "Piedra colocada en [" + row + "," + col + "]");
    }

    @FXML
    private void onPassTurn() {
        if (gameEnded) {
            statusLabel.setText("Partida finalizada");
            return;
        }

        isPlayer1Turn = !isPlayer1Turn;
        updateCurrentTurn();
        statusLabel.setText((isPlayer1Turn ? player1Name : player2Name) + " pasó su turno");
        LOGGER.log(Level.INFO, "Turno pasado");
    }

    @FXML
    private void onUndo() {
        statusLabel.setText("Deshacer no disponible en esta versión");
        LOGGER.log(Level.INFO, "Deshacer solicitado");
    }

    @FXML
    private void onSurrender() {
        gameEnded = true;
        String winner = isPlayer1Turn ? player2Name : player1Name;
        String loser = isPlayer1Turn ? player1Name : player2Name;
        statusLabel.setText(winner + " ganó. " + loser + " se rindió");
        LOGGER.log(Level.INFO, loser + " se rindió. Ganador: " + winner);
    }

    @FXML
    private void onBackToMenu() {
        stopGameTimer();
        LOGGER.log(Level.INFO, "Volviendo al menú");
        // Castear a Stage para poder cerrar la ventana
        javafx.stage.Stage stage = (javafx.stage.Stage) boardCanvas.getScene().getWindow();
        stage.close();
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
                    if (isPlayer1Turn) {
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

    public void setPlayerNames(String player1, String player2) {
        this.player1Name = player1 != null ? player1 : "Jugador 1";
        this.player2Name = player2 != null ? player2 : "Jugador 2";
        if (player1NameLabel != null) {
            updatePlayerInfo();
        }
    }

    public void setInitialScores(int score1, int score2) {
        this.player1Score = score1;
        this.player2Score = score2;
        if (player1ScoreLabel != null) {
            updateScores();
        }
    }
}



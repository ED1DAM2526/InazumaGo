package es.iesquevedo.controller;

import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Move;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import es.iesquevedo.exception.InvalidMoveException;
import es.iesquevedo.exception.PlayerNotInTurnException;

import javafx.animation.AnimationTimer;
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

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameController {
    private static final Logger LOGGER = Logger.getLogger(GameController.class.getName());
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

    private String player1Name = "Jugador 1";
    private String player2Name = "Jugador 2";
    private String matchingPlayerName = "Jugador";
    private long player1TimeMs = 0;
    private long player2TimeMs = 0;
    private AnimationTimer gameTimer;
    private long lastTime = 0;
    private boolean gameEnded = false;
    
    // Lógica real del juego
    private Game game;
    private InazumaGoMoveValidator moveValidator;
    private boolean moveInProgress = false;

    @FXML
    public void initialize() {
        LOGGER.log(Level.INFO, "GameController inicializado");
        
        // Inicializar validador de movimientos
        moveValidator = new InazumaGoMoveValidator();
        
        // Crear juego con dos jugadores locales
        Player player1 = new Player("1", player1Name);
        Player player2 = new Player("2", player2Name);
        
        game = new Game("Local Game", player1);
        game.addPlayer(player2);
        game.start();
        
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
        // Calcular puntuación simple: contar piedras por color
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
        
        // Komi (ventaja blanca): 5.5
        double blackScore = blackStones;
        double whiteScore = whiteStones + 5.5;
        
        player1ScoreLabel.setText(String.format("Puntos: %.1f", blackScore));
        player2ScoreLabel.setText(String.format("Puntos: %.1f", whiteScore));
    }

    private void updateCurrentTurn() {
        Player currentPlayer = game.getCurrentPlayer();
        String turn = currentPlayer != null ? currentPlayer.getName() : player1Name;
        currentTurnLabel.setText("Turno: " + turn);
    }

    private void drawBoard() {
        GraphicsContext gc = boardCanvas.getGraphicsContext2D();
        double boardStartX = getBoardStartX();
        double boardStartY = getBoardStartY();
        double boardEndX = getBoardEndX();
        double boardEndY = getBoardEndY();
        double boardSpan = (BOARD_SIZE - 1) * CELL_SIZE;

        // Fondo de madera con degradado ligero (más realista)
        LinearGradient wood = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#D2A679")),
                new Stop(0.5, Color.web("#C37E3A")),
                new Stop(1, Color.web("#B06A2E"))
        );
        gc.setFill(wood);
        gc.fillRect(0, 0, boardCanvas.getWidth(), boardCanvas.getHeight());

        // Granulado / vetas finas
        gc.setStroke(Color.color(0.15, 0.07, 0.03, 0.06));
        gc.setLineWidth(0.8);
        for (int i = 0; i < (int) boardCanvas.getWidth(); i += 6) {
            double offset = (i % 24) * 0.3;
            gc.beginPath();
            gc.moveTo(0, (i + offset) % boardCanvas.getHeight());
            gc.bezierCurveTo(boardCanvas.getWidth() * 0.25, (i + offset + 8) % boardCanvas.getHeight(),
                    boardCanvas.getWidth() * 0.75, (i + offset - 12 + boardCanvas.getHeight()) % boardCanvas.getHeight(),
                    boardCanvas.getWidth(), (i + offset) % boardCanvas.getHeight());
            gc.stroke();
        }

        // Marco exterior con esquinas redondeadas perceptuales
        gc.setStroke(Color.color(0.12, 0.06, 0.03, 1.0));
        gc.setLineWidth(5);
        double outerX = boardStartX - 4;
        double outerY = boardStartY - 4;
        double outerW = boardSpan + 8;
        double outerH = boardSpan + 8;
        gc.strokeRoundRect(outerX, outerY, outerW, outerH, 12, 12);

        // Marco interior menos intenso
        gc.setStroke(Color.color(0.36, 0.18, 0.08, 0.7));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(boardStartX - 1, boardStartY - 1,
                boardSpan + 2, boardSpan + 2, 8, 8);

        // Líneas del tablero, ligeramente suavizadas con doble trazo (sombra + línea)
        for (int i = 0; i < BOARD_SIZE; i++) {
            double y = getIntersectionY(i);
            double x = getIntersectionX(i);

            // sombra de la línea horizontal
            gc.setStroke(Color.color(0, 0, 0, 0.12));
            gc.setLineWidth(2.0);
            gc.strokeLine(boardStartX + 0.8, y + 0.8, boardEndX + 0.8, y + 0.8);

            // línea principal
            gc.setStroke(Color.color(0.06, 0.03, 0.01, 1.0));
            gc.setLineWidth(1.2);
            gc.strokeLine(boardStartX, y, boardEndX, y);

            // sombra vertical
            gc.setStroke(Color.color(0, 0, 0, 0.12));
            gc.setLineWidth(2.0);
            gc.strokeLine(x + 0.8, boardStartY + 0.8, x + 0.8, boardEndY + 0.8);

            // línea vertical principal
            gc.setStroke(Color.color(0.06, 0.03, 0.01, 1.0));
            gc.setLineWidth(1.2);
            gc.strokeLine(x, boardStartY, x, boardEndY);
        }

        // Coordenadas alrededor del tablero
        drawCoordinates(gc, boardStartX, boardStartY, boardEndX, boardEndY);

        // Puntos hoshizashi mejor integrados
        gc.setFill(Color.color(0.04, 0.02, 0.01, 1.0));
        int[] stars = {2, 4, 6};
        for (int i : stars) {
            for (int j : stars) {
                double px = getIntersectionX(i);
                double py = getIntersectionY(j);
                gc.fillOval(px - 3, py - 3, 6, 6);
                gc.setStroke(Color.color(0, 0, 0, 0.18));
                gc.setLineWidth(0.6);
                gc.strokeOval(px - 4, py - 4, 8, 8);
            }
        }

        // Dibujar piedras del tablero real (Board)
        Board board = game.getBoard();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int cell = board.getCell(r, c);
                if (cell == 1) { // Negro
                    drawStone(gc, r, c, 1);
                } else if (cell == 2) { // Blanco
                    drawStone(gc, r, c, 2);
                }
            }
        }
    }

    private void drawStone(GraphicsContext gc, int row, int col, int stoneColor) {
        double x = getIntersectionX(col);
        double y = getIntersectionY(row);
        double radius = CELL_SIZE / 2 - 3;

        if (stoneColor == 1) { // Negro
            // sombra proyectada
            gc.setFill(Color.color(0, 0, 0, 0.28));
            gc.fillOval(x - radius + 2, y - radius + 3, radius * 1.9, radius * 1.9);

            // gradiente radial para dar volumen
            RadialGradient blackGrad = new RadialGradient(
                    45, 0.1,
                    x - radius * 0.15, y - radius * 0.2,
                    radius * 1.05, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#6e6e6e")),
                    new Stop(0.4, Color.web("#222222")),
                    new Stop(1.0, Color.web("#000000"))
            );
            gc.setFill(blackGrad);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // highlight suave (brillo)
            RadialGradient shine = new RadialGradient(
                    45, 0.2,
                    x - radius * 0.25, y - radius * 0.3,
                    radius * 0.6, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.color(1, 1, 1, 0.9)),
                    new Stop(0.4, Color.color(1, 1, 1, 0.25)),
                    new Stop(1.0, Color.color(1, 1, 1, 0.0))
            );
            gc.setGlobalAlpha(1.0);
            gc.setFill(shine);
            gc.fillOval(x - radius * 0.2, y - radius * 0.25, radius * 1.2, radius * 1.2);

            // borde ligero para definición
            gc.setStroke(Color.color(0, 0, 0, 0.45));
            gc.setLineWidth(0.6);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        } else if (stoneColor == 2) { // Blanco
            // piedra blanca: sombra más suave
            gc.setFill(Color.color(0.06, 0.06, 0.06, 0.14));
            gc.fillOval(x - radius + 1.8, y - radius + 2.6, radius * 1.95, radius * 1.95);

            // base con degradado radial cálido
            RadialGradient whiteGrad = new RadialGradient(
                    45, 0.12,
                    x - radius * 0.12, y - radius * 0.18,
                    radius * 1.05, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#ffffff")),
                    new Stop(0.6, Color.web("#f2f2f2")),
                    new Stop(1.0, Color.web("#d1d1d1"))
            );
            gc.setFill(whiteGrad);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // borde suave gris
            gc.setStroke(Color.color(0.6, 0.6, 0.6, 0.6));
            gc.setLineWidth(0.9);
            gc.strokeOval(x - radius + 0.4, y - radius + 0.4, radius * 2 - 0.8, radius * 2 - 0.8);

            // highlights blancos
            RadialGradient whiteShine = new RadialGradient(
                    45, 0.2,
                    x - radius * 0.2, y - radius * 0.25,
                    radius * 0.5, false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.color(1, 1, 1, 0.95)),
                    new Stop(0.6, Color.color(1, 1, 1, 0.45)),
                    new Stop(1.0, Color.color(1, 1, 1, 0.0))
            );
            gc.setFill(whiteShine);
            gc.fillOval(x - radius * 0.15, y - radius * 0.18, radius * 0.95, radius * 0.95);

            // textura sutil
            gc.setFill(Color.color(0.8, 0.8, 0.8, 0.05));
            gc.fillOval(x - radius + 2, y - radius + 2, radius * 1.4, radius * 1.4);
        }
    }

    @FXML
    private void onBoardClick(MouseEvent event) {
        if (gameEnded || moveInProgress) {
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

    public void initGame(String gameId, String playerName) {
        this.matchingPlayerName = playerName != null ? playerName : "Jugador";
        this.player1Name = this.matchingPlayerName;
        this.player2Name = "Oponente";

        if (game != null && game.getPlayers().size() >= 2) {
            game.getPlayers().get(0).setName(this.player1Name);
            game.getPlayers().get(1).setName(this.player2Name);
        }

        updatePlayerInfo();
        LOGGER.log(Level.INFO, "Partida iniciada - GameID: " + gameId + " - Jugador: " + playerName);
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void placeStone(int row, int col) {
        if (moveInProgress || gameEnded) {
            return;
        }
        
        moveInProgress = true;
        
        try {
            Player currentPlayer = game.getCurrentPlayer();
            
            if (currentPlayer == null) {
                statusLabel.setText("Error: sin jugador actual");
                moveInProgress = false;
                return;
            }
            
            // Crear movimiento con playerId
            Move move = new Move(currentPlayer.getId(), row, col);
            
            // Validar movimiento
            moveValidator.validateMove(game, move);
            
            // Guardar estado previo para detectar repetición
            Board previousBoardState = game.getBoard().clone();
            
            // Ejecutar movimiento en el tablero
            Board board = game.getBoard();
            int playerColor = (game.getCurrentPlayerIndex() == 0) ? 1 : 2; // 1=Negro, 2=Blanco
            board.placeStone(row, col, playerColor);
            
            // Capturar grupos enemigos sin libertades
            int capturedCount = board.captureGroupsWithoutLiberties();
            
            // Registrar movimiento
            move.setCapturedCount(capturedCount);
            game.getMoves().add(move);
            
            // Detectar repetición de posición (se ignora la reversión por ahora)
            if (game.getLastBoardState() != null && board.equals(game.getLastBoardState())) {
                statusLabel.setText("Movimiento rechazado: repetición de posición");
                moveInProgress = false;
                return;
            }
            
            game.setLastBoardState(previousBoardState);
            
            // Movimiento exitoso
            statusLabel.setText("Movimiento realizado" + (capturedCount > 0 ? " (" + capturedCount + " piedras capturadas)" : ""));
            
            // Cambiar turno
            game.nextTurn();
            game.resetConsecutivePasses();
            
            updatePlayerInfo();
            drawBoard();
            moveInProgress = false;
            
            LOGGER.log(Level.INFO, "Piedra colocada en [" + row + "," + col + "], capturadas: " + capturedCount);
            
        } catch (InvalidMoveException | PlayerNotInTurnException ex) {
            statusLabel.setText("Movimiento inválido: " + ex.getMessage());
            moveInProgress = false;
            LOGGER.log(Level.WARNING, "Error en movimiento: " + ex.getMessage());
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            moveInProgress = false;
            LOGGER.log(Level.SEVERE, "Error inesperado en movimiento: " + ex.getMessage());
        }
    }

    @FXML
    private void onPassTurn() {
        if (gameEnded || moveInProgress) {
            return;
        }

        moveInProgress = true;
        
        try {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer == null) {
                statusLabel.setText("Error: sin jugador actual");
                moveInProgress = false;
                return;
            }
            
            // Crear movimiento de PASE
            Move move = new Move(currentPlayer.getId(), true); // PASE
            
            // Validar (pasadas sin validación especial)
            moveValidator.validateMove(game, move);
            
            // Registrar pase
            game.getMoves().add(move);
            game.incrementConsecutivePasses();
            
            // Cambiar turno
            game.nextTurn();
            
            statusLabel.setText(currentPlayer.getName() + " pasó su turno. Pases consecutivos: " + game.getConsecutivePasses());
            
            // Verificar doble pase = fin de partida
            if (game.getConsecutivePasses() >= 2) {
                endGame();
            }
            
            updateCurrentTurn();
            moveInProgress = false;
            
            LOGGER.log(Level.INFO, "Turno pasado. Pases consecutivos: " + game.getConsecutivePasses());
        } catch (Exception ex) {
            statusLabel.setText("Error al pasar: " + ex.getMessage());
            moveInProgress = false;
        }
    }

    @FXML
    private void onUndo() {
        statusLabel.setText("Deshacer no disponible en esta versión");
        LOGGER.log(Level.INFO, "Deshacer solicitado");
    }

    @FXML
    private void onSurrender() {
        gameEnded = true;
        game.setState(GameState.FINISHED);
        
        Player winner = game.getPlayers().get((game.getCurrentPlayerIndex() + 1) % 2);
        Player loser = game.getCurrentPlayer();
        
        game.setWinnerPlayerId(winner.getId());
        
        statusLabel.setText(winner.getName() + " ganó. " + loser.getName() + " se rindió");
        LOGGER.log(Level.INFO, loser.getName() + " se rindió. Ganador: " + winner.getName());
    }

    @FXML
    private void onBackToMenu() {
        stopGameTimer();
        LOGGER.log(Level.INFO, "Volviendo al emparejamiento");
        goToMatchingScreen();
    }

    private void goToMatchingScreen() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/MatchingScreen.fxml"));
            javafx.scene.Parent root = loader.load();

            es.iesquevedo.ui.MatchingScreenController controller = loader.getController();
            controller.setGameService(new GameServiceImpl());
            controller.startMatching(new Player(matchingPlayerName, matchingPlayerName));

            javafx.stage.Stage stage = (javafx.stage.Stage) boardCanvas.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 500, 300));
            stage.setTitle("InazumaGo - Emparejamiento");
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al volver al emparejamiento", e);
            statusLabel.setText("Error al volver al emparejamiento");
        }
    }
    
    private void endGame() {
        gameEnded = true;
        game.setState(GameState.FINISHED);
        
        // Calcular puntuación final
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
        
        String winner = blackScore > whiteScore ? player1Name : player2Name;
        String result = String.format("Partida finalizada. %s gana %.1f - %.1f", 
            winner, Math.max(blackScore, whiteScore), Math.min(blackScore, whiteScore));
        
        statusLabel.setText(result);
        LOGGER.log(Level.INFO, result);
        
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

    public void setPlayerNames(String player1, String player2) {
        this.player1Name = player1 != null ? player1 : "Jugador 1";
        this.player2Name = player2 != null ? player2 : "Jugador 2";
        if (player1NameLabel != null) {
            updatePlayerInfo();
        }
    }

    public void setInitialScores(int score1, int score2) {
        // Puntuación inicial se calcula del Board, no se establece manualmente
        if (player1ScoreLabel != null) {
            updateScores();
        }
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

    private void drawCoordinates(GraphicsContext gc, double boardStartX, double boardStartY, double boardEndX, double boardEndY) {
        String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "J"};
        gc.setFill(Color.color(0.22, 0.12, 0.05, 0.9));
        gc.setFont(Font.font("System", 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);

        double offset = 18;
        for (int i = 0; i < BOARD_SIZE; i++) {
            double x = getIntersectionX(i);
            double y = getIntersectionY(i);

            // Letras arriba y abajo
            gc.fillText(letters[i], x, boardStartY - offset);
            gc.fillText(letters[i], x, boardEndY + offset);

            // Números a izquierda y derecha
            String number = String.valueOf(BOARD_SIZE - i);
            gc.fillText(number, boardStartX - offset, y);
            gc.fillText(number, boardEndX + offset, y);
        }
    }
}




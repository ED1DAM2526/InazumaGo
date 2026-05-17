package es.iesquevedo.ui;

import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.service.GameService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameController {

    @FXML
    private Label gameIdLabel;

    @FXML
    private Label turnLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private GridPane boardGrid;

    @FXML
    private Button resignButton;

    private GameService gameService;
    private String gameId;
    private String playerId;
    private GameStateDto currentState;
    private ScheduledExecutorService pollExecutor;

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void initGame(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;

        // Load initial state
        loadGameState();

        // Poll for updates every 2 seconds (simplified; Firebase listeners would be better)
        pollExecutor = Executors.newSingleThreadScheduledExecutor();
        pollExecutor.scheduleAtFixedRate(() -> loadGameState(), 0, 2, TimeUnit.SECONDS);
    }

    private void loadGameState() {
        gameService.getGameState(gameId).thenAccept(state -> {
            Platform.runLater(() -> updateUI(state));
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Error: " + ex.getMessage()));
            return null;
        });
    }

    private void updateUI(GameStateDto state) {
        this.currentState = state;
        gameIdLabel.setText("Game: " + state.getGameId());
        turnLabel.setText("Turn: " + state.getCurrentTurnPlayerId());
        statusLabel.setText("Status: " + state.getStatus());

        if (state.getWinnerId() != null) {
            statusLabel.setText("Winner: " + state.getWinnerId());
            resignButton.setDisable(true);
        }

        // Highlight if it's your turn
        if (playerId.equals(state.getCurrentTurnPlayerId()) && "IN_PROGRESS".equals(state.getStatus())) {
            turnLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            turnLabel.setText("YOUR TURN!");
        } else {
            turnLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onResign() {
        gameService.resign(gameId, playerId).thenAccept(ignored -> {
            Platform.runLater(() -> {
                statusLabel.setText("You resigned. Game over.");
                resignButton.setDisable(true);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Resign failed: " + ex.getMessage()));
            return null;
        });
    }

    // Example: board cell click (simple 3x3 board for demonstration)
    @FXML
    private void onCellClick(javafx.event.ActionEvent event) {
        Button cell = (Button) event.getSource();
        String cellId = cell.getId(); // e.g., "cell_0_0"
        String[] parts = cellId.split("_");
        int row = Integer.parseInt(parts[1]);
        int col = Integer.parseInt(parts[2]);

        if (!playerId.equals(currentState.getCurrentTurnPlayerId())) {
            statusLabel.setText("Not your turn!");
            return;
        }

        if (!"IN_PROGRESS".equals(currentState.getStatus())) {
            statusLabel.setText("Game is not in progress");
            return;
        }

        // Build move payload
        MovePayload payload = new MovePayload();
        // TODO: Set actual move data (depends on your game logic)

        gameService.submitMove(gameId, playerId, payload).thenAccept(state -> {
            Platform.runLater(() -> {
                updateUI(state);
                cell.setText("X"); // Example: mark the move
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                statusLabel.setText("Move rejected: " + ex.getMessage());
                // Rollback visual feedback
                cell.setStyle("-fx-background-color: red;");
            });
            return null;
        });
    }

    public void cleanup() {
        if (pollExecutor != null) {
            pollExecutor.shutdown();
        }
    }
}
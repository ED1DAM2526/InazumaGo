package es.iesquevedo.ui;

import es.iesquevedo.dto.GameStateDto;
import es.iesquevedo.dto.MovePayload;
import es.iesquevedo.service.GameService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
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

    // Map to store button references for each cell
    private Map<String, Button> cellButtons = new HashMap<>();

    // Store last attempted move for rollback
    private String lastAttemptedMoveKey = null;
    private String lastAttemptedPlayerId = null;

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void initGame(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;

        // Initialize cell buttons
        initializeBoardButtons();

        // Load initial state
        loadGameState();

        // Poll for updates every 2 seconds
        pollExecutor = Executors.newSingleThreadScheduledExecutor();
        pollExecutor.scheduleAtFixedRate(() -> loadGameState(), 0, 2, TimeUnit.SECONDS);
    }

    private void initializeBoardButtons() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String buttonId = "cell_" + row + "_" + col;
                Button cell = (Button) boardGrid.lookup("#" + buttonId);
                if (cell != null) {
                    cellButtons.put(buttonId, cell);
                    final int r = row;
                    final int c = col;
                    cell.setOnAction(event -> onCellClick(r, c, cell));
                }
            }
        }
    }

    private void loadGameState() {
        gameService.getGameState(gameId).thenAccept(state -> {
            Platform.runLater(() -> updateUI(state));
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Error loading game: " + ex.getMessage()));
            return null;
        });
    }

    private void updateUI(GameStateDto state) {
        this.currentState = state;
        gameIdLabel.setText("Game: " + state.getGameId());
        turnLabel.setText("Turn: " + state.getCurrentTurnPlayerId());
        statusLabel.setText("Status: " + state.getStatus());

        if (state.getWinnerId() != null) {
            statusLabel.setText("🏆 Winner: " + state.getWinnerId());
            resignButton.setDisable(true);
        }

        // Update board from server state (sync)
        if (state.getBoardState() != null) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    String cellValue = state.getBoardState().getCell(row, col);
                    String buttonId = "cell_" + row + "_" + col;
                    Button cell = cellButtons.get(buttonId);
                    if (cell != null && !cellValue.isEmpty()) {
                        String symbol = cellValue.equals(playerId) ? "X" : "O";
                        cell.setText(symbol);
                        cell.setDisable(true);
                    }
                }
            }
        }

        // Highlight if it's your turn
        if (playerId.equals(state.getCurrentTurnPlayerId()) && "IN_PROGRESS".equals(state.getStatus())) {
            turnLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            turnLabel.setText("YOUR TURN! 👈");
        } else {
            turnLabel.setStyle("-fx-text-fill: red;");
            if (!"IN_PROGRESS".equals(state.getStatus())) {
                turnLabel.setText("Game Over");
            } else {
                turnLabel.setText("Waiting for opponent...");
            }
        }

        // Clear any error message after successful update
        if (statusLabel.getText().startsWith("❌")) {
            statusLabel.setText("Status: " + state.getStatus());
        }
    }

    private void onCellClick(int row, int col, Button cell) {
        // Check if it's your turn
        if (!playerId.equals(currentState.getCurrentTurnPlayerId())) {
            showTemporaryError("❌ Not your turn!");
            return;
        }

        if (!"IN_PROGRESS".equals(currentState.getStatus())) {
            showTemporaryError("❌ Game is not in progress");
            return;
        }

        // Check if cell is already occupied
        if (currentState.getBoardState() != null &&
                !currentState.getBoardState().getCell(row, col).isEmpty()) {
            showTemporaryError("❌ Cell already occupied!");
            return;
        }

        // --- OPTIMISTIC UPDATE ---
        // Save current cell text for potential rollback
        String previousText = cell.getText();
        String symbol = "X"; // Player who moves is X
        cell.setText(symbol);
        cell.setDisable(true);

        // Store last attempted move for rollback
        lastAttemptedMoveKey = row + "," + col;
        lastAttemptedPlayerId = playerId;

        // Show pending status
        statusLabel.setText("⏳ Sending move...");

        // Build move payload
        MovePayload payload = new MovePayload(row, col, playerId);

        // --- SEND MOVE TO SERVER ---
        gameService.submitMove(gameId, playerId, payload).thenAccept(state -> {
            Platform.runLater(() -> {
                // Move accepted - update UI with server state
                updateUI(state);
                statusLabel.setText("✅ Move accepted!");
                lastAttemptedMoveKey = null;

                // Clear success message after 2 seconds
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                    Platform.runLater(() -> {
                        if (statusLabel.getText().equals("✅ Move accepted!")) {
                            statusLabel.setText("Status: " + (currentState != null ? currentState.getStatus() : ""));
                        }
                    });
                }).start();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                // --- ROLLBACK ON REJECTION ---
                String errorMessage = ex.getMessage();

                // Rollback the optimistic update
                cell.setText(previousText);
                cell.setDisable(false);
                cell.setStyle("-fx-background-color: #ffcccc;");

                // Show clear feedback
                if (errorMessage != null && errorMessage.contains("Not your turn")) {
                    statusLabel.setText("❌ Move rejected: Not your turn!");
                } else if (errorMessage != null && errorMessage.contains("cell occupied")) {
                    statusLabel.setText("❌ Move rejected: Cell already taken!");
                } else if (errorMessage != null && errorMessage.contains("out of bounds")) {
                    statusLabel.setText("❌ Move rejected: Invalid position!");
                } else {
                    statusLabel.setText("❌ Move rejected: " + (errorMessage != null ? errorMessage : "Unknown error"));
                }

                // Reset cell style after 2 seconds
                new Thread(() -> {
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                    Platform.runLater(() -> {
                        cell.setStyle("");
                        if (statusLabel.getText().startsWith("❌")) {
                            statusLabel.setText("Status: " + (currentState != null ? currentState.getStatus() : ""));
                        }
                    });
                }).start();

                // Reload actual game state to ensure sync
                loadGameState();
            });
            return null;
        });
    }

    private void showTemporaryError(String message) {
        String originalText = statusLabel.getText();
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                statusLabel.setText(originalText);
                statusLabel.setStyle("");
            });
        }).start();
    }

    @FXML
    private void onResign() {
        gameService.resign(gameId, playerId).thenAccept(ignored -> {
            Platform.runLater(() -> {
                statusLabel.setText("You resigned. Game over.");
                resignButton.setDisable(true);
                loadGameState();
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> statusLabel.setText("Resign failed: " + ex.getMessage()));
            return null;
        });
    }

    public void cleanup() {
        if (pollExecutor != null) {
            pollExecutor.shutdown();
        }
    }
}
package es.iesquevedo.ui;

import es.iesquevedo.controller.GameController;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchingScreenController {

    private static final Logger LOGGER = Logger.getLogger(MatchingScreenController.class.getName());

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button searchButton;

    private GameService gameService = new GameServiceImpl();
    private Player currentPlayer;
    private Timer timer;
    private int elapsedSeconds = 0;
    private CompletableFuture<Game> currentSearch;
    private volatile boolean searching = true;

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void startMatching(Player player) {
        this.currentPlayer = player;
        if (this.gameService == null) {
            this.gameService = new GameServiceImpl();
        }
        updateReadyState();
    }

    @FXML
    private void onSearchClicked() {
        if (currentPlayer == null) {
            statusLabel.setText("No hay jugador autenticado");
            return;
        }
        if (searching) {
            return;
        }
        searching = true;
        elapsedSeconds = 0;
        startTimer();
        startSearch();
    }

    private void startTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    elapsedSeconds++;
                    timeLabel.setText("Tiempo de espera: " + elapsedSeconds + "s");
                });
            }
        }, 1000, 1000);
    }

    private void startSearch() {
        statusLabel.setText("Buscando jugador disponible...");
        if (searchButton != null) {
            searchButton.setDisable(true);
        }
        if (cancelButton != null) {
            cancelButton.setText("Cancelar");
        }

        currentSearch = CompletableFuture.supplyAsync(() -> {
            try {
                int waitTime = 3000 + (int)(Math.random() * 5000);
                Thread.sleep(waitTime);
                if (!searching) return null;

                String gameName = "Partida_" + System.currentTimeMillis();
                return gameService.createGame(gameName, currentPlayer);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Search interrupted", e);
                return null;
            }
        });

        currentSearch.thenAccept(game -> {
            if (game != null && searching) {
                Platform.runLater(() -> {
                    stopTimer();
                    searching = false;
                    if (searchButton != null) {
                        searchButton.setDisable(false);
                    }
                    statusLabel.setText("¡Oponente encontrado!");
                    navigateToGame(game.getId());
                });
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (searching) {
                    statusLabel.setText("Error en la búsqueda. Intenta de nuevo.");
                    searching = false;
                    if (searchButton != null) {
                        searchButton.setDisable(false);
                    }
                    cancelButton.setText("Volver");
                }
            });
            return null;
        });
    }

    private void navigateToGame(String gameId) {
        try {
            searching = false;
            if (currentSearch != null) {
                currentSearch.cancel(true);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            String playerName = currentPlayer != null ? currentPlayer.getName() : "Jugador";
            gameController.initGame(gameId, playerName);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("InazumaGo - Partida: " + gameId);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading game screen", e);
            statusLabel.setText("Error al cargar la partida");
        }
    }

    @FXML
    private void onCancel() {
        if (searching) {
            searching = false;
            if (currentSearch != null) {
                currentSearch.cancel(true);
            }
            stopTimer();
            if (searchButton != null) {
                searchButton.setDisable(false);
            }
            goBackToMainMenu();
            return;
        }
        goBackToMainMenu();
    }

    private void updateReadyState() {
        searching = false;
        statusLabel.setText(currentPlayer != null
                ? "Listo para buscar partida. Pulsa 'Buscar partida'."
                : "Cargando jugador...");
        if (timeLabel != null) {
            timeLabel.setText("Tiempo de espera: 0s");
        }
        if (searchButton != null) {
            searchButton.setDisable(false);
        }
        if (cancelButton != null) {
            cancelButton.setText("Volver");
        }
    }

    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle("InazumaGo - Login");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error returning to main menu", e);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
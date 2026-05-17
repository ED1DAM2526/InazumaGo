package es.iesquevedo.ui;

import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.repository.MainRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainScreenController {

    @FXML
    private ListView<String> playersListView;

    @FXML
    private Button randomMatchButton;

    @FXML
    private Button createGameButton;

    @FXML
    private Button joinGameButton;

    private GameService gameService;
    private MainRepository repository;
    private String currentPlayerId;

    public void setService(MainRepository repository) {
        this.repository = repository;
        this.currentPlayerId = "player-" + System.currentTimeMillis();
        this.gameService = new GameServiceImpl(repository);
        loadMockPlayers();
    }

    @FXML
    public void initialize() {
        // Initialization if needed
    }

    private void loadMockPlayers() {
        List<String> mockPlayers = Arrays.asList("Jugador1", "Jugador2", "Jugador3", "Jugador4", "Jugador5");
        playersListView.getItems().setAll(mockPlayers);
    }

    @FXML
    private void onCreateGame() {
        if (gameService == null) {
            showError("Error", "Servicio no disponible");
            return;
        }

        gameService.createGame(currentPlayerId).thenAccept(gameId -> {
            Platform.runLater(() -> {
                showInfo("Partida Creada", "ID de partida: " + gameId);
                navigateToGame(gameId, currentPlayerId);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Error", "No se pudo crear la partida: " + ex.getMessage()));
            return null;
        });
    }

    @FXML
    private void onJoinGameDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Unirse a Partida");
        dialog.setHeaderText("Ingresa el ID de la partida");
        dialog.setContentText("Game ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(gameId -> {
            joinGame(gameId);
        });
    }

    private void joinGame(String gameId) {
        gameService.joinGame(gameId, currentPlayerId).thenAccept(gameState -> {
            Platform.runLater(() -> {
                showInfo("Unido", "Te has unido a la partida: " + gameId);
                navigateToGame(gameId, currentPlayerId);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> showError("Error", "No se pudo unir a la partida: " + ex.getMessage()));
            return null;
        });
    }

    private void navigateToGame(String gameId, String playerId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            gameController.setGameService(gameService);
            gameController.initGame(gameId, playerId);

            Stage stage = (Stage) createGameButton.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setTitle("InazumaGo - Partida: " + gameId);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "No se pudo cargar la pantalla de juego");
        }
    }

    @FXML
    private void onRandomMatch() {
        showInfo("Emparejamiento Aleatorio", "Buscando oponente...");
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            Platform.runLater(() -> onCreateGame());
        }).start();
    }

    @FXML
    private void onRefreshPlayers() {
        loadMockPlayers();
        showInfo("Actualizar", "Lista de jugadores actualizada.");
    }

    @FXML
    private void onChallengeSelected() {
        String selected = playersListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Atención", "Debes seleccionar un jugador de la lista.");
        } else {
            showInfo("Retar", "Has retado a " + selected + ". Esperando respuesta...");
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
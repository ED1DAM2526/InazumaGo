package es.iesquevedo.ui;

import es.iesquevedo.service.MainService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;

import java.util.Arrays;
import java.util.List;

public class MainScreenController {

    @FXML
    private ListView<String> playersListView;

    @FXML
    private Button randomMatchButton;

    private MainService mainService;

    // Called by MainGUI after loading FXML
    public void setService(MainService mainService) {
        this.mainService = mainService;
        // Once service is set, we could load real data from Firebase
        // For now, load mock players
        loadMockPlayers();
    }

    @FXML
    public void initialize() {
        // Initialization if needed; actual data loading happens in setService
    }

    private void loadMockPlayers() {
        // TODO: Replace with real data from Firebase via MainService / PlayerRepository
        List<String> mockPlayers = Arrays.asList("Jugador1", "Jugador2", "Jugador3", "Jugador4", "Jugador5");
        playersListView.getItems().setAll(mockPlayers);
    }

    @FXML
    private void onRandomMatch() {
        // Simulate random matchmaking
        showInfo("Emparejamiento Aleatorio", "Buscando oponente...");
        // In a real implementation, you would call a matchmaking service and navigate to game screen.
        // For demo, we simulate a found match after 1 second.
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                showInfo("Partida encontrada", "Has sido emparejado con OponenteAleatorio. ¡Comienza la partida!");
                // Here you would load the game board screen
            });
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
            // Here you would send a challenge via Firebase
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
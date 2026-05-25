package es.iesquevedo.controller;

import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.ui.MatchingScreenController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenuController {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    @FXML
    private Label playerEmailLabel;

    private String playerEmail;

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
        String displayName = buildDisplayName(playerEmail);
        playerEmailLabel.setText("👤 " + displayName);
    }

    @FXML
    private void onMatchmakingClicked() {
        navigateToMatching(playerEmail);
    }

    @FXML
    private void onLocalGameClicked() {
        navigateToLocalGame();
    }

    @FXML
    private void onExitClicked() {
        Stage stage = (Stage) playerEmailLabel.getScene().getWindow();
        stage.close();
        LOGGER.log(Level.INFO, "Aplicación cerrada");
    }

    @FXML
    private void onLogoutClicked() {
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginRoot = loginLoader.load();

            LoginController loginController = loginLoader.getController();
            loginController.setAuthService(new es.iesquevedo.service.impl.AuthServiceImpl());

            Scene scene = playerEmailLabel.getScene();
            scene.setRoot(loginRoot);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Login");

            LOGGER.log(Level.INFO, "Logout realizado, volviendo a login");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver a login: " + e.getMessage());
        }
    }

    private void navigateToMatching(String playerEmail) {
        try {
            FXMLLoader matchingLoader = new FXMLLoader(getClass().getResource("/fxml/MatchingScreen.fxml"));
            Parent matchingRoot = matchingLoader.load();

            MatchingScreenController matchingController = matchingLoader.getController();
            String playerName = buildDisplayName(playerEmail);
            matchingController.startMatching(new Player(playerEmail, playerName));

            Scene scene = playerEmailLabel.getScene();
            scene.setRoot(matchingRoot);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Emparejamiento");

            LOGGER.log(Level.INFO, "Navegado a emparejamiento desde menú principal");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al navegar a emparejamiento: " + e.getMessage());
        }
    }

    private void navigateToLocalGame() {
        try {
            FXMLLoader localGameLoader = new FXMLLoader(getClass().getResource("/fxml/LocalGame.fxml"));
            Parent localGameRoot = localGameLoader.load();

            LocalGameController localGameController = localGameLoader.getController();
            localGameController.initializeLocalGame();

            Scene scene = playerEmailLabel.getScene();
            scene.setRoot(localGameRoot);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Partida Local");
            stage.setMaximized(true);

            LOGGER.log(Level.INFO, "Navegado a partida local");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al navegar a partida local: " + e.getMessage());
        }
    }

    private String buildDisplayName(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Jugador";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}


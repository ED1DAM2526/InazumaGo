package es.iesquevedo.controller;

import es.iesquevedo.config.AppConfig;
import es.iesquevedo.config.AppState;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.AuthService;
import es.iesquevedo.service.impl.AuthServiceImpl;
import es.iesquevedo.service.impl.GameServiceImpl;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la pantalla de Registro.
 * Permite crear nuevos usuarios en Firebase Authentication.
 */
public class RegisterController {
    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backButton;
    @FXML
    private Label messageLabel;

    private AuthService authService;

    @FXML
    public void initialize() {
        // Inicializar AuthService con Firebase Auth real
        this.authService = new AuthServiceImpl();
        messageLabel.setText("");
    }

    @FXML
    public void onRegisterButtonClicked(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validaciones básicas
        if (email.isEmpty()) {
            showError("Por favor ingresa un email");
            return;
        }

        if (password.isEmpty()) {
            showError("Por favor ingresa una contraseña");
            return;
        }

        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            return;
        }

        // Validar email básico
        if (!email.contains("@")) {
            showError("Email inválido");
            return;
        }

        registerButton.setDisable(true);
        showInfo("Registrando...");

        // Llamar al signup de AuthService
        if (authService instanceof AuthServiceImpl) {
            AuthServiceImpl authImpl = (AuthServiceImpl) authService;
            authImpl.signup(email, password)
                    .thenAccept(token -> {
                        Platform.runLater(() -> {
                            AppState.getInstance().setAuthToken(token);
                            AppState.getInstance().setCurrentUserEmail(email);
                            showInfo("¡Registro exitoso! Bienvenido " + email);
                            navigateToMatching(email);
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            LOGGER.log(Level.WARNING, "Signup error: " + ex.getMessage());
                            showError("Error en registro: " + ex.getMessage());
                            registerButton.setDisable(false);
                        });
                        return null;
                    });
        }
    }

    @FXML
    public void onBackButtonClicked(ActionEvent event) {
        navigateToLogin();
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #cc0000;");
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: #0066cc;");
        messageLabel.setText(message);
    }

    private void navigateToMatching(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MatchingScreen.fxml"));
            Scene scene = new Scene(loader.load(), 500, 300);
            es.iesquevedo.ui.MatchingScreenController controller = loader.getController();
            String playerName = buildDisplayName(email);
            controller.startMatching(new Player(email, playerName));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("InazumaGo - Emparejamiento");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Matching: " + e.getMessage());
            showError("Error al abrir la pantalla de emparejamiento");
        }
    }

    private String buildDisplayName(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Jugador";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 400, 300);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to Login: " + e.getMessage());
        }
    }
}




package es.iesquevedo.controller;

import es.iesquevedo.app.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    // Constructor para tests (inyección del servicio)
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, rellena todos los campos.");
            return;
        }

        try {
            String token = authService.wait;

            // Guardar token en memoria
            AppState.getInstance().saveToken(token, email);

            showSuccess("✅ Login exitoso. Bienvenido, " + email);

        } catch (Exception e) {
            showError("❌ Error: " + e.getMessage());
        }
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }

    // Para tests: exponer el label
    public String getStatusText() {
        return statusLabel.getText();
    }

}
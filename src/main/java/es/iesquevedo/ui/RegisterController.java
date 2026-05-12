package es.iesquevedo.ui;

import es.iesquevedo.service.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label mensajeLabel;

    private AuthService authService;
    private LoginController loginController;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    @FXML
    private void handleRegistro() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty()) {
            mensajeLabel.setText("El email no puede estar vacío.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!email.contains("@")) {
            mensajeLabel.setText("El email no es válido.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (password.isEmpty()) {
            mensajeLabel.setText("La contraseña no puede estar vacía.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (password.length() < 6) {
            mensajeLabel.setText("La contraseña debe tener al menos 6 caracteres.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!password.equals(confirmPassword)) {
            mensajeLabel.setText("Las contraseñas no coinciden.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String token = authService.register(email, password);

        if (token != null) {
            mensajeLabel.setText("¡Cuenta creada exitosamente! Redirigiendo...");
            mensajeLabel.setStyle("-fx-text-fill: green;");

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        loginController.volverAlLogin();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            mensajeLabel.setText("El email ya está registrado o hubo un error.");
            mensajeLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleCancelar() {
        loginController.volverAlLogin();
    }
}

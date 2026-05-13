package es.iesquevedo.ui;

import es.iesquevedo.service.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label mensajeLabel;

    private AuthService authService;

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        try {
            String token = authService.login(email, password);

            if (token != null) {
                // Login correcto → abrir pantalla principal
                abrirPantallaPrincipal(token);
            } else {
                mensajeLabel.setText("Email o contraseña incorrectos.");
            }
        } catch (Exception e) {
            mensajeLabel.setText("Error al iniciar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCrearCuenta() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/register.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 350));
            stage.setTitle("InazumaGo — Crear Cuenta");

            RegisterController registerCtrl = loader.getController();
            registerCtrl.setAuthService(authService);
            registerCtrl.setLoginController(this);

        } catch (Exception e) {
            mensajeLabel.setText("Error al abrir la pantalla de registro.");
            e.printStackTrace();
        }
    }

    public void volverAlLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
            stage.setTitle("InazumaGo — Login");

            LoginController loginCtrl = loader.getController();
            loginCtrl.setAuthService(authService);

        } catch (Exception e) {
            mensajeLabel.setText("Error al volver al login.");
            e.printStackTrace();
        }
    }

    private void abrirPantallaPrincipal(String token) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml")
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 400, 300));
            stage.setTitle("InazumaGo — Principal");

            MainFXController mainCtrl = loader.getController();
            mainCtrl.init(authService);

        } catch (Exception e) {
            mensajeLabel.setText("Error al abrir la pantalla principal.");
            e.printStackTrace();
        }
    }
}

package es.iesquevedo.controller;

import es.iesquevedo.config.AppState;
import es.iesquevedo.service.auth.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para la pantalla de login.
 * Maneja el flujo de autenticación y muestra feedback al usuario.
 */
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private AuthService authService;
    private AppState appState;

    /**
     * Constructor sin parámetros necesario para FXML.
     */
    public LoginController() {
        this.appState = AppState.getInstance();
    }

    /**
     * Inyecta el servicio de autenticación.
     * 
     * @param authService servicio de autenticación
     */
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        LOGGER.log(Level.INFO, "AuthService inyectado en LoginController");
    }

    /**
     * Manejador del botón "Iniciar sesión".
     * Valida campos, llama a AuthService y actualiza el estado.
     */
    @FXML
    public void onLoginClicked() {
        LOGGER.log(Level.INFO, "Botón login clickeado");

        // Obtener valores de los campos
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validar campos no vacíos
        if (email == null || email.trim().isEmpty()) {
            updateStatus("El email no puede estar vacío", "error");
            LOGGER.log(Level.WARNING, "Intento de login sin email");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            updateStatus("La contraseña no puede estar vacía", "error");
            LOGGER.log(Level.WARNING, "Intento de login sin contraseña");
            return;
        }

        // Intentar login
        try {
            if (authService == null) {
                updateStatus("Error: Servicio de autenticación no disponible", "error");
                LOGGER.log(Level.SEVERE, "AuthService es null en LoginController");
                return;
            }

            String token = authService.login(email, password);

            // Guardar token en AppState
            appState.setAuthToken(token);
            appState.setCurrentUserEmail(email);

            // Mostrar éxito
            updateStatus("✓ Login exitoso. Bienvenido, " + email, "success");
            LOGGER.log(Level.INFO, "Login exitoso para: " + email);

            // Limpiar campos
            emailField.clear();
            passwordField.clear();

            // Nota: En una app real aquí navegerías a la pantalla principal
            // Por ahora solo mostramos el mensaje de éxito

        } catch (Exception e) {
            updateStatus("✗ Error de login: " + e.getMessage(), "error");
            LOGGER.log(Level.WARNING, "Error en login: " + e.getMessage());
        }
    }

    /**
     * Manejador del botón "Limpiar" (opcional).
     */
    @FXML
    public void onClearClicked() {
        emailField.clear();
        passwordField.clear();
        statusLabel.setText("");
        LOGGER.log(Level.INFO, "Campos limpiados");
    }

    /**
     * Actualiza el label de estado con mensaje y color.
     * 
     * @param message mensaje a mostrar
     * @param type tipo de mensaje: "success" (verde) o "error" (rojo)
     */
    private void updateStatus(String message, String type) {
        statusLabel.setText(message);

        if ("success".equals(type)) {
            statusLabel.setTextFill(Color.GREEN);
            LOGGER.log(Level.INFO, "Mensaje de éxito: " + message);
        } else if ("error".equals(type)) {
            statusLabel.setTextFill(Color.RED);
            LOGGER.log(Level.INFO, "Mensaje de error: " + message);
        } else {
            statusLabel.setTextFill(Color.BLACK);
        }
    }
}


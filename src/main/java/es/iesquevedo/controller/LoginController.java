package es.iesquevedo.controller;

import es.iesquevedo.config.AppState;
import es.iesquevedo.service.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;
import es.iesquevedo.util.EmailUtils;

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

    @FXML
    private Button registerButton;

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

        // Validar formato de email (asegura que contenga '@' y estructura básica)
        if (!EmailUtils.isValidEmail(email)) {
            updateStatus("El email no tiene un formato válido. Ej: usuario@ejemplo.com", "error");
            LOGGER.log(Level.WARNING, "Intento de login con email inválido: " + email);
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

            // Navegar a la pantalla de emparejamiento
            navigateToMatching(email);

        } catch (Exception e) {
            updateStatus("✗ Error de login: " + e.getMessage(), "error");
            LOGGER.log(Level.WARNING, "Error en login: " + e.getMessage());
        }
    }

    /**
     * Navega a la pantalla de emparejamiento después del login exitoso.
     *
     * @param playerEmail email del jugador autenticado
     */
    private void navigateToMatching(String playerEmail) {
        try {
            FXMLLoader matchingLoader = new FXMLLoader(getClass().getResource("/fxml/MatchingScreen.fxml"));
            Parent matchingRoot = matchingLoader.load();

            es.iesquevedo.ui.MatchingScreenController matchingController = matchingLoader.getController();
            String playerName = buildDisplayName(playerEmail);
            matchingController.startMatching(new es.iesquevedo.model.Player(playerEmail, playerName));
            
            Scene scene = emailField.getScene();
            scene.setRoot(matchingRoot);
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Emparejamiento");
            
            LOGGER.log(Level.INFO, "Navegado a pantalla de emparejamiento para: " + playerEmail);
        } catch (Exception e) {
            updateStatus("✗ Error al cargar pantalla de emparejamiento: " + e.getMessage(), "error");
            LOGGER.log(Level.SEVERE, "Error al cargar MatchingScreen.fxml: " + e.getMessage());
        }
    }

    private String buildDisplayName(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Jugador";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
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

    /**
     * Navega a la pantalla de registro.
     */
    @FXML
    public void onRegisterClicked() {
        try {
            FXMLLoader registerLoader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent registerRoot = registerLoader.load();
            
            Scene scene = emailField.getScene();
            scene.setRoot(registerRoot);
            Stage stage = (Stage) scene.getWindow();
            stage.setTitle("InazumaGo - Registro");
            
            LOGGER.log(Level.INFO, "Navegado a pantalla de registro");
        } catch (Exception e) {
            updateStatus("✗ Error al cargar pantalla de registro: " + e.getMessage(), "error");
            LOGGER.log(Level.SEVERE, "Error al cargar Register.fxml: " + e.getMessage());
        }
    }
}


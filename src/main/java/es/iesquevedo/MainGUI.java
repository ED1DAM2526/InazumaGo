package es.iesquevedo;

import es.iesquevedo.controller.LoginController;
import es.iesquevedo.service.auth.AuthServiceMock;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainGUI extends Application {
    private static final Logger LOGGER = Logger.getLogger(MainGUI.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Cargar FXML de Login con su controlador
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = (Parent) loader.load();
            
            // Obtener el controlador después de cargar el FXML
            LoginController loginController = loader.getController();
            
            // Inyectar el servicio de autenticación (mock para desarrollo)
            loginController.setAuthService(new AuthServiceMock());

            // Crear escena y mostrar ventana
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("InazumaGo - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Aplicación JavaFX iniciada exitosamente (Login)");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al iniciar la aplicación", e);
        }
    }
}

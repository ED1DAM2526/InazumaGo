package es.iesquevedo;

import es.iesquevedo.controller.LoginController;
import es.iesquevedo.service.impl.AuthServiceImpl;
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
            // Cargar pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            // Configurar LoginController
            LoginController controller = loader.getController();
            controller.setAuthService(new AuthServiceImpl());

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setTitle("InazumaGo - Login");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

            LOGGER.log(Level.INFO, "Aplicación JavaFX iniciada - Pantalla de login cargada");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al iniciar la aplicación", e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package es.iesquevedo;

import es.iesquevedo.config.AppConfig;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.ui.MainScreenController;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainScreen.fxml"));
            Parent root = loader.load();

            MainScreenController controller = loader.getController();
            String firebaseUrl = System.getenv("FIREBASE_URL");
            var repository = AppConfig.createMainRepository(firebaseUrl);
            var mainService = new MainServiceImpl(repository);
            controller.setService(mainService);

            Scene scene = new Scene(root, 700, 500);
            primaryStage.setTitle("InazumaGo - Pantalla Principal");
            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.log(Level.INFO, "Aplicación JavaFX iniciada - Pantalla principal cargada");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al iniciar la aplicación", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

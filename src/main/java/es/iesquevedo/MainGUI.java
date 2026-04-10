package es.iesquevedo;

import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
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
            // Cargar FXML con su controlador
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            Parent root = (Parent) loader.load();
            
            // Obtener el controlador después de cargar el FXML
            MainController mainController = loader.getController();
            
            // Configuración de servicios
            String firebaseUrl = System.getenv("FIREBASE_URL");
            var repository = AppConfig.createMainRepository(firebaseUrl);
            var mainService = new MainServiceImpl(repository);
            
            // Inyectar dependencias en el controlador
            mainController.setService(mainService);


            // Crear escena y mostrar ventana
            Scene scene = new Scene(root, 600, 400);
            primaryStage.setTitle("InazumaGo");
            primaryStage.setScene(scene);
            primaryStage.show();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Aplicación JavaFX iniciada exitosamente");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al iniciar la aplicación", e);
        }
    }
}

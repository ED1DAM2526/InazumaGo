package es.iesquevedo;

E3-US1-T4
import es.iesquevedo.ui.UIAdapter;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Configura la app como en Main.java
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var controller = new MainController(service);
        var ui = new UIAdapter(controller);

        // Crea la UI
        Label label = new Label(ui.greet());
        label.setId("greetingLabel"); // Para que el test lo encuentre
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("InazumaGo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.HealthController;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.ui.HealthUIAdapter;
import es.iesquevedo.ui.UIAdapter;
import es.iesquevedo.util.DateUtils;
import javafx.application.Application;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        // Modo console: si se proporciona argumento "console", ejecutar en modo CLI
        if (args.length > 0 && "console".equals(args[0])) {
            runConsoleMode();
            System.exit(0);
        } else {
            // Modo GUI: lanzar aplicación JavaFX
            try {
                Application.launch(MainGUI.class, args);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al iniciar JavaFX", e);
                System.exit(1);
            }
        }
    }

    private static void runConsoleMode() {
        // Configuración mínima: preferir URL de Firebase desde la variable de entorno FIREBASE_URL
        String firebaseUrl = System.getenv("FIREBASE_URL");

        // Crear repositorio (Firebase si FIREBASE_URL está definido, sino InMemory)
        var repository = AppConfig.createMainRepository(firebaseUrl);

        // Crear servicio y controlador
        var service = new MainServiceImpl(repository);
        var mainController = new MainController();
        mainController.setService(service);

        // Adaptadores UI
        var ui = new UIAdapter(mainController);
        var healthController = new HealthController();
        var healthUi = new HealthUIAdapter(healthController);

        // Uso simple: saludar y comprobar estado
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(String.format("%s %s", DateUtils.nowIso(), ui.greet()));
            LOGGER.info(String.format("%s Health: %s", DateUtils.nowIso(), healthUi.health()));
        }
      dev
    }
}
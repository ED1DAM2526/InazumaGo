package es.iesquevedo;

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
        var mainController = new MainController(service);

        // Adaptadores UI
        var ui = new UIAdapter(mainController);
        var healthController = new HealthController();
        var healthUi = new HealthUIAdapter(healthController);

        // Uso simple: saludar y comprobar estado
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "{0} {1}", new Object[]{DateUtils.nowIso(), ui.greet()});
            LOGGER.log(Level.INFO, "{0} Health: {1}", new Object[]{DateUtils.nowIso(), healthUi.health()});
        }
    }
}




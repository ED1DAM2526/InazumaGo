package es.iesquevedo;

import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.HealthController;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.ui.HealthUIAdapter;
import es.iesquevedo.ui.UIAdapter;
import es.iesquevedo.util.DateUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
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
        // Comprobar explícitamente si el nivel está habilitado y usar el formateo incorporado
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, "{0} {1}", new Object[]{DateUtils.nowIso(), ui.greet()});
            LOGGER.log(Level.INFO, "{0} Health: {1}", new Object[]{DateUtils.nowIso(), healthUi.health()});
        }
    }
}

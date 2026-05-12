package es.iesquevedo.controller;

import es.iesquevedo.exception.ApiError;
import es.iesquevedo.exception.MainErrorHandler;
import es.iesquevedo.service.MainService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final MainErrorHandler errorHandler = new MainErrorHandler();

    private MainService mainService;

    @FXML
    private Label salutoLabel;

    public MainController() {
        // Constructor sin parámetros necesario para FXML
        this.mainService = null;
    }

    public void setService(MainService mainService) {
        this.mainService = mainService;
        // Reinicializar después de establecer el servicio
        initialize();
    }

    @FXML
    public void initialize() {
        // Este método se llama automáticamente después de que FXML carga los componentes
        if (mainService != null && salutoLabel != null) {
            try {
                String saludo = mainService.greet();
                salutoLabel.setText(saludo);
                LOGGER.info("Saludo establecido: " + saludo);
            } catch (RuntimeException e) {
                ApiError apiError = errorHandler.toApiError(e);
                LOGGER.log(Level.SEVERE, "Error al obtener saludo del servicio", e);
                salutoLabel.setText(formatApiError(apiError));
            }
        } else if (salutoLabel != null) {
            salutoLabel.setText("Servicio no disponible");
        }
    }

    public String greet() {
        return mainService != null ? mainService.greet() : "Servicio no disponible";
    }

    static String formatApiError(ApiError apiError) {
        return "Error [" + apiError.getCode() + "]: " + apiError.getMessage();
    }

    public void setSalutoLabel(Label label) {
        this.salutoLabel = label;
    }
}

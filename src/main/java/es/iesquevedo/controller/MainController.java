package es.iesquevedo.controller;

import es.iesquevedo.service.MainService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.logging.Logger;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    
    private final MainService mainService;

    @FXML
    private Label salutoLabel;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    public MainController() {
        // Constructor sin parámetros necesario para FXML
        this.mainService = null;
    }

    @FXML
    public void initialize() {
        // Este método se llama automáticamente después de que FXML carga los componentes
        if (mainService != null && salutoLabel != null) {
            try {
                String saludo = mainService.greet();
                salutoLabel.setText(saludo);
                LOGGER.info("Saludo establecido: " + saludo);
            } catch (Exception e) {
                LOGGER.severe("Error al obtener saludo del servicio: " + e.getMessage());
                salutoLabel.setText("Error: no se pudo cargar el saludo");
            }
        } else {
            if (salutoLabel != null) {
                salutoLabel.setText("Servicio no disponible");
            }
        }
    }

    public String greet() {
        return mainService != null ? mainService.greet() : "Servicio no disponible";
    }

    public void setSalutoLabel(Label label) {
        this.salutoLabel = label;
    }
}


package es.iesquevedo.ui;

import es.iesquevedo.controller.MainController;

/**
 * Adaptador simple entre la capa de UI (JavaFX/CLI) y los controladores de la aplicación.
 * Mantiene el código existente de `MainController` y delega en él.
 */
public class UIAdapter {
    private final MainController mainController;

    public UIAdapter(MainController mainController) {
        this.mainController = mainController;
    }

    public String greet() {
        return mainController.greet();
    }
}

package es.iesquevedo.ui;

import es.iesquevedo.controller.HealthController;

public class HealthUIAdapter {
    private final HealthController healthController;

    public HealthUIAdapter(HealthController healthController) {
        this.healthController = healthController;
    }

    public String health() {
        return healthController.health();
    }
}

package es.iesquevedo.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthControllerTest {
    @Test
    public void health_shouldReturnOK() {
        HealthController controller = new HealthController();
        assertEquals("OK", controller.health());
    }
}


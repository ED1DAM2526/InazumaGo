package es.iesquevedo.ui;

import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Casos de prueba visual para validar:
 * 1. Arranque correcto del UIAdapter
 * 2. Presencia y contenido del saludo (label)
 */
class UIAdapterTest {

    /**
     * TC-001: Verificar que el UIAdapter se inicializa correctamente
     */
    @Test
    void testUIAdapterInitializes() {
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var mainController = new MainController(service);
        var ui = new UIAdapter(mainController);

        assertNotNull(ui, "El UIAdapter no debe ser nulo");
    }

    /**
     * TC-002: Verificar que el saludo (label) no es nulo
     */
    @Test
    void testUIAdapterGreetingIsNotNull() {
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var mainController = new MainController(service);
        var ui = new UIAdapter(mainController);

        String greeting = ui.greet();
        assertNotNull(greeting, "El saludo del label no debe ser nulo");
    }

    /**
     * TC-003: Verificar que el saludo contiene texto esperado
     */
    @Test
    void testUIAdapterGreetingContainsHello() {
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var mainController = new MainController(service);
        var ui = new UIAdapter(mainController);

        String greeting = ui.greet();
        assertTrue(greeting.contains("Hello"),
                "El saludo debe contener 'Hello'. Contenido actual: " + greeting);
    }

    /**
     * TC-004: Verificar que el saludo no está vacío
     */
    @Test
    void testUIAdapterGreetingIsNotEmpty() {
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var mainController = new MainController(service);
        var ui = new UIAdapter(mainController);

        String greeting = ui.greet();
        assertFalse(greeting.trim().isEmpty(),
                "El saludo no debe estar vacío");
    }
}

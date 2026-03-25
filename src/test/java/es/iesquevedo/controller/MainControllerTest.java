package es.iesquevedo.controller;

import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainControllerTest {
    @Test
    public void greet_shouldDelegateToService() {
        // Arrange: Crear dependencias (repositorio en memoria, servicio, controlador)
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var controller = new MainController(service);

        // Act: Llamar al método del controlador
        String result = controller.greet();

        // Assert: Verificar que delega correctamente al servicio
        assertEquals("Hello, InazumaGoPrevio!", result);
    }
}

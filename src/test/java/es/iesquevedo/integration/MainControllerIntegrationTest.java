package es.iesquevedo.integration;

import es.iesquevedo.controller.MainController;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.MainService;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainControllerIntegrationTest {

    @Test
    void testGreetDelegatesToService() {
        MainRepository repository = new InMemoryMainRepository();
        MainService mainService = new MainServiceImpl(repository);
        MainController mainController = new MainController();
        mainController.setService(mainService);

        assertEquals("Hello, InazumaGoPrevio!", mainController.greet());
    }

    @Test
    void testGreetWhenServiceIsNull() {
        MainController emptyController = new MainController();
        String result = emptyController.greet();
        assertEquals("Servicio no disponible", result);
    }
}
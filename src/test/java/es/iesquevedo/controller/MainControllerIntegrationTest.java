package es.iesquevedo.controller;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.MainService;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainControllerIntegrationTest {

    @Test
    void greet_shouldDelegateToMainService() {
        MainRepository repository = new InMemoryMainRepository();
        MainService service = new MainServiceImpl(repository);
        MainController controller = new MainController();
        controller.setService(service);

        assertEquals("Hello, InazumaGoPrevio!", controller.greet());
    }
}


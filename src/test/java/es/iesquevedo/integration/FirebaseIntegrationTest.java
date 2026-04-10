package es.iesquevedo.integration;

import es.iesquevedo.config.AppConfig;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FirebaseIntegrationTest {

    @Test
    void testGreetWithInMemoryRepository() {
        MainRepository repo = AppConfig.createInMemoryRepository();
        MainController controller = new MainController();
        controller.setService(new MainServiceImpl(repo));

        String greeting = controller.greet();

        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }
}

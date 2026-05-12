package es.iesquevedo.service;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainServiceTest {
    @Test
    public void status_shouldReturnGreetingWithDefaultName() {
        MainRepository repo = new InMemoryMainRepository();
        MainService service = new MainServiceImpl(repo);

        String greeting = service.status();

        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }
}

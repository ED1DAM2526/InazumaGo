package es.iesquevedo.service;

import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public class MainServiceTest {

    private MainService service;

    @BeforeEach
    void setUp() {
        MainRepository repository = new InMemoryMainRepository();
        service = new MainServiceImpl(repository);
    }

    @Test
    void testGreetReturnsHelloWithDefaultName() {
        String greeting = service.greet();
        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }

    @Test
    public void greet_shouldThrowNotFound_whenDefaultNameIsMissing() {
        MainRepository repo = new InMemoryMainRepository() {
            @Override
            public String findDefaultName() {
                return null;
            }
        };
        MainService service = new MainServiceImpl(repo);

        NotFoundException exception = assertThrows(NotFoundException.class, service::greet);

        assertEquals("Default player name not found", exception.getMessage());
    }
}

    @Test
    void testGreetNotNull() {
        String greeting = service.greet();
        assertNotNull(greeting);
    }

    @Test
    void testGreetContainsHello() {
        String greeting = service.greet();
        assertTrue(greeting.contains("Hello"));
    }
}
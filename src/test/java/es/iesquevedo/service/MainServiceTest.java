package es.iesquevedo.service;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
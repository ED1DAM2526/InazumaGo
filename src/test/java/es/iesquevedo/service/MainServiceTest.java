package es.iesquevedo.service;

import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@DisplayName("Pruebas unitarias de MainServiceImpl")
public class MainServiceTest {

    private MainService service;
    private MainRepository repository;

    @Mock
    private MainRepository mockRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new InMemoryMainRepository();
        service = new MainServiceImpl(repository);
    }

    @DisplayName("Debe retornar saludo con nombre por defecto")
    @Test
    void testGreetReturnsHelloWithDefaultName() {
        String greeting = service.greet();
        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }

    @DisplayName("Debe lanzar NotFoundException cuando falta el nombre por defecto")
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

    @DisplayName("El saludo no debe ser nulo")
    @Test
    void testGreetNotNull() {
        String greeting = service.greet();
        assertNotNull(greeting);
    }

    @DisplayName("El saludo debe contener 'Hello'")
    @Test
    void testGreetContainsHello() {
        String greeting = service.greet();
        assertTrue(greeting.contains("Hello"));
    }

    // ===== Tests con Mocks (Mockito) =====

    @DisplayName("MainServiceImpl debe llamar a findDefaultName() en el repositorio")
    @Test
    void testGreetCallsRepositoryFindDefaultName() {
        when(mockRepository.findDefaultName()).thenReturn("TestPlayer");
        MainService serviceWithMock = new MainServiceImpl(mockRepository);

        String greeting = serviceWithMock.greet();

        verify(mockRepository, times(1)).findDefaultName();
        assertEquals("Hello, TestPlayer!", greeting);
    }

    @DisplayName("MainServiceImpl debe manejar nombres nulos del repositorio lanzando NotFoundException")
    @Test
    void testGreetWithMockThrowsNotFoundWhenRepositoryReturnsNull() {
        when(mockRepository.findDefaultName()).thenReturn(null);
        MainService serviceWithMock = new MainServiceImpl(mockRepository);

        NotFoundException exception = assertThrows(NotFoundException.class, serviceWithMock::greet);

        verify(mockRepository, times(1)).findDefaultName();
        assertEquals("Default player name not found", exception.getMessage());
    }

    @DisplayName("MainServiceImpl debe usar el nombre devuelto por el repositorio mock")
    @Test
    void testGreetUsesRepositoryMockValue() {
        String customName = "CustomTestPlayer";
        when(mockRepository.findDefaultName()).thenReturn(customName);
        MainService serviceWithMock = new MainServiceImpl(mockRepository);

        String greeting = serviceWithMock.greet();

        assertEquals("Hello, " + customName + "!", greeting);
        verify(mockRepository).findDefaultName();
    }
}
package es.iesquevedo.service;

import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.AfterEach;
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
class MainServiceTest {

    private MainService mainService;
    private AutoCloseable closeable;

    @Mock
    private MainRepository mockRepository;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        MainRepository repository = new InMemoryMainRepository();
        mainService = new MainServiceImpl(repository);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @DisplayName("Debe retornar saludo con nombre por defecto")
    @Test
    void testGreetReturnsHelloWithDefaultName() {
        String greeting = mainService.greet();
        assertEquals("Hello, InazumaGoPrevio!", greeting);
    }

    @DisplayName("Debe lanzar NotFoundException cuando falta el nombre por defecto")
    @Test
    void greet_shouldThrowNotFound_whenDefaultNameIsMissing() {
        MainRepository repo = new InMemoryMainRepository() {
            @Override
            public String findDefaultName() {
                return null;
            }
        };
        MainService testService = new MainServiceImpl(repo);

        NotFoundException exception = assertThrows(NotFoundException.class, testService::greet);

        assertEquals("Default player name not found", exception.getMessage());
    }

    @DisplayName("El saludo no debe ser nulo")
    @Test
    void testGreetNotNull() {
        String greeting = mainService.greet();
        assertNotNull(greeting);
    }

    @DisplayName("El saludo debe contener 'Hello'")
    @Test
    void testGreetContainsHello() {
        String greeting = mainService.greet();
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
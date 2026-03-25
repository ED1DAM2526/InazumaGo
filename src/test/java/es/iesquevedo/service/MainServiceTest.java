package es.iesquevedo.service;

import es.iesquevedo.exception.NotFoundException;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Suite de tests unitarios para MainServiceImpl usando Mockito.
 * Los tests verifican:
 * - Comportamiento correcto con repositorio que devuelve datos válidos
 * - Comportamiento con excepciones (null del repositorio)
 * - Interacción correcta entre servicio y repositorio (verify)
 */
@ExtendWith(MockitoExtension.class)
public class MainServiceTest {

    @Mock
    private MainRepository mockRepository;

    private MainService service;

    @BeforeEach
    public void setUp() {
        // Inicializar el servicio con el mock del repositorio
        service = new MainServiceImpl(mockRepository);
    }

    // ========== TESTS CON MOCK (Mockito.when) ==========

    /**
     * Test: Verificar que greet() retorna el nombre correcto cuando el repositorio devuelve un valor válido.
     * Mock: Repository devuelve "InazumaGoPrevio"
     */
    @Test
    public void greet_shouldReturnGreetingWithName_whenRepositoryReturnsValidName() {
        // Arrange: Configurar el mock para devolver un nombre específico
        when(mockRepository.findDefaultName()).thenReturn("TestPlayer");

        // Act: Llamar al método del servicio
        String result = service.greet();

        // Assert: Verificar el resultado
        assertEquals("Hello, TestPlayer!", result);

        // Verify: Verificar que el repositorio fue llamado exactamente una vez
        verify(mockRepository).findDefaultName();
    }

    /**
     * Test: Verificar que greet() devuelve el mensaje por defecto con el nombre del repositorio.
     * Mock: Repository devuelve "InazumaGoPrevio"
     */
    @Test
    public void greet_shouldReturnGreetingWithDefaultName_whenRepositoryReturnsInazumaGoPrevio() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("InazumaGoPrevio");

        // Act
        String result = service.greet();

        // Assert
        assertEquals("Hello, InazumaGoPrevio!", result);

        // Verify: Asegurarse de que findDefaultName fue invocado
        verify(mockRepository).findDefaultName();
    }

    /**
     * Test: Verificar que greet() lanza NotFoundException cuando el repositorio devuelve null.
     * Mock: Repository devuelve null
     */
    @Test
    public void greet_shouldThrowNotFoundException_whenRepositoryReturnsNull() {
        // Arrange: Configurar el mock para devolver null
        when(mockRepository.findDefaultName()).thenReturn(null);

        // Act & Assert: Verificar que se lanza NotFoundException
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            service.greet();
        });

        // Verify: Validar el mensaje de la excepción
        assertEquals("Default name not found in repository", exception.getMessage());

        // Verify: Asegurarse de que findDefaultName fue invocado
        verify(mockRepository).findDefaultName();
    }

    /**
     * Test: Verificar que greet() lanza NotFoundException con mensaje correcto.
     */
    @Test
    public void greet_shouldThrowNotFoundExceptionWithCorrectMessage_whenNameIsNull() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn(null);

        // Act & Assert
        try {
            service.greet();
        } catch (NotFoundException e) {
            assertEquals("Default name not found in repository", e.getMessage());
        }
    }

    /**
     * Test: Verificar que greet() funciona con nombres especiales/vacíos (después de null check).
     * Mock: Repository devuelve una cadena especial
     */
    @Test
    public void greet_shouldReturnGreetingWithSpecialName_whenRepositoryReturnsSpecialCharacters() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("Go-Player_123");

        // Act
        String result = service.greet();

        // Assert
        assertEquals("Hello, Go-Player_123!", result);

        // Verify
        verify(mockRepository).findDefaultName();
    }

    /**
     * Test: Verificar que el repositorio es llamado exactamente una vez por invocación de greet().
     * Mock: Repository devuelve un nombre válido
     */
    @Test
    public void greet_shouldCallRepositoryExactlyOnce() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("Player1");

        // Act: Llamar greet() una sola vez
        service.greet();

        // Verify: Asegurarse de que findDefaultName fue invocado exactamente una vez
        verify(mockRepository).findDefaultName();
    }

    /**
     * Test: Verificar que greet() NO tiene efectos secundarios (no modifica el repositorio).
     * Mock: Repository devuelve un nombre
     */
    @Test
    public void greet_shouldNotModifyRepository_onlyReadsData() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("ReadOnlyPlayer");

        // Act
        service.greet();

        // Verify: Solo findDefaultName debe ser llamado, no otros métodos del repositorio
        verify(mockRepository).findDefaultName();
    }

    // ========== TESTS CON IMPLEMENTACIÓN REAL (InMemory) PARA COMPARACIÓN ==========

    /**
     * Test de integración simple: Usar InMemoryMainRepository en lugar de mock.
     * Propósito: Validar comportamiento real sin mocks.
     */
    @Test
    public void greet_shouldReturnGreetingWithInMemoryRepository() {
        // Arrange: Usar InMemoryMainRepository real (no mock)
        MainRepository realRepository = new InMemoryMainRepository();
        MainService realService = new MainServiceImpl(realRepository);

        // Act
        String result = realService.greet();

        // Assert
        assertEquals("Hello, InazumaGoPrevio!", result);
    }

    /**
     * Test: Verificar que el servicio es independiente de la implementación del repositorio.
     * Usar un mock alternativo que devuelve un nombre diferente.
     */
    @Test
    public void greet_shouldWorkWithDifferentRepositoryImplementations() {
        // Arrange: Mock que devuelve un nombre personalizado
        when(mockRepository.findDefaultName()).thenReturn("CustomGame");

        // Act
        String result = service.greet();

        // Assert: El resultado debe ser correcto independientemente de la implementación
        assertEquals("Hello, CustomGame!", result);
    }
}



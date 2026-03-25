package es.iesquevedo.controller;

import es.iesquevedo.exception.ApiError;
import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.service.impl.MainServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

/**
 * Suite de tests unitarios para MainController.
 * Verifica:
 * - Delegación al servicio
 * - Manejo seguro de excepciones (greetSafe)
 * - Mapeo de excepciones a ApiError
 */
@ExtendWith(MockitoExtension.class)
public class MainControllerExceptionTest {

    @Mock
    private MainRepository mockRepository;

    /**
     * Test: greet() delega correctamente al servicio.
     */
    @Test
    public void greet_shouldDelegateToService() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("TestPlayer");
        var service = new MainServiceImpl(mockRepository);
        var controller = new MainController(service);

        // Act
        String result = controller.greet();

        // Assert
        assertEquals("Hello, TestPlayer!", result);
    }

    /**
     * Test: greetSafe() retorna string cuando el servicio funciona correctamente.
     */
    @Test
    public void greetSafe_shouldReturnStringWhenServiceSucceeds() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn("Player");
        var service = new MainServiceImpl(mockRepository);
        var controller = new MainController(service);

        // Act
        Object result = controller.greetSafe();

        // Assert
        assertInstanceOf(String.class, result);
        assertEquals("Hello, Player!", result);
    }

    /**
     * Test: greetSafe() retorna ApiError cuando el servicio lanza NotFoundException.
     */
    @Test
    public void greetSafe_shouldReturnApiErrorWhenServiceThrowsNotFoundException() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn(null);
        var service = new MainServiceImpl(mockRepository);
        var controller = new MainController(service);

        // Act
        Object result = controller.greetSafe();

        // Assert
        assertInstanceOf(ApiError.class, result);
        ApiError error = (ApiError) result;
        assertEquals("Default name not found in repository", error.getMessage());
        assertEquals(404, error.getCode());
    }

    /**
     * Test: Verificar que ApiError tiene el código correcto para NotFoundException.
     */
    @Test
    public void greetSafe_shouldMapNotFoundExceptionTo404Code() {
        // Arrange
        when(mockRepository.findDefaultName()).thenReturn(null);
        var service = new MainServiceImpl(mockRepository);
        var controller = new MainController(service);

        // Act
        Object result = controller.greetSafe();

        // Assert
        assertInstanceOf(ApiError.class, result);
        ApiError error = (ApiError) result;
        assertEquals(404, error.getCode());
    }

    /**
     * Test: greetSafe() maneja excepciones genéricas.
     */
    @Test
    public void greetSafe_shouldHandleGenericException() {
        // Arrange: Mock que lanza excepción genérica
        MainRepository brokenRepo = () -> {
            throw new RuntimeException("Database error");
        };
        var service = new MainServiceImpl(brokenRepo);
        var controller = new MainController(service);

        // Act
        Object result = controller.greetSafe();

        // Assert
        assertInstanceOf(ApiError.class, result);
        ApiError error = (ApiError) result;
        assertEquals(500, error.getCode());
    }
}

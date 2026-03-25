package es.iesquevedo.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite de tests unitarios para GlobalExceptionHandler.
 * Verifica que el manejador global transforma excepciones a ApiError correctamente.
 */
public class GlobalExceptionHandlerTest {

    /**
     * Test: NotFoundException debe retornar ApiError con código 404.
     */
    @Test
    public void handle_shouldReturnApiErrorWith404ForNotFoundException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Resource not found");

        // Act
        ApiError error = GlobalExceptionHandler.handle(exception);

        // Assert
        assertNotNull(error);
        assertEquals("Resource not found", error.getMessage());
        assertEquals(404, error.getCode());
    }

    /**
     * Test: IllegalArgumentException debe retornar ApiError con código 400.
     */
    @Test
    public void handle_shouldReturnApiErrorWith400ForIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid name");

        // Act
        ApiError error = GlobalExceptionHandler.handle(exception);

        // Assert
        assertNotNull(error);
        assertEquals(400, error.getCode());
        assertTrue(error.getMessage().contains("Invalid argument"));
    }

    /**
     * Test: RuntimeException debe retornar ApiError con código 500.
     */
    @Test
    public void handle_shouldReturnApiErrorWith500ForRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database error");

        // Act
        ApiError error = GlobalExceptionHandler.handle(exception);

        // Assert
        assertNotNull(error);
        assertEquals(500, error.getCode());
        assertTrue(error.getMessage().contains("Internal error"));
    }

    /**
     * Test: Excepción desconocida debe retornar ApiError con código 500.
     */
    @Test
    public void handle_shouldReturnApiErrorWith500ForUnknownException() {
        // Arrange
        Exception exception = new Exception("Unknown error");

        // Act
        ApiError error = GlobalExceptionHandler.handle(exception);

        // Assert
        assertNotNull(error);
        assertEquals(500, error.getCode());
    }

    /**
     * Test: getHttpStatusCode debe retornar 404 para NotFoundException.
     */
    @Test
    public void getHttpStatusCode_shouldReturn404ForNotFoundException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Not found");

        // Act
        int code = GlobalExceptionHandler.getHttpStatusCode(exception);

        // Assert
        assertEquals(404, code);
    }

    /**
     * Test: getHttpStatusCode debe retornar 400 para IllegalArgumentException.
     */
    @Test
    public void getHttpStatusCode_shouldReturn400ForIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid");

        // Act
        int code = GlobalExceptionHandler.getHttpStatusCode(exception);

        // Assert
        assertEquals(400, code);
    }

    /**
     * Test: getHttpStatusCode debe retornar 500 para RuntimeException.
     */
    @Test
    public void getHttpStatusCode_shouldReturn500ForRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Error");

        // Act
        int code = GlobalExceptionHandler.getHttpStatusCode(exception);

        // Assert
        assertEquals(500, code);
    }

    /**
     * Test: isKnownException debe retornar true para NotFoundException.
     */
    @Test
    public void isKnownException_shouldReturnTrueForNotFoundException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Not found");

        // Act
        boolean isKnown = GlobalExceptionHandler.isKnownException(exception);

        // Assert
        assertTrue(isKnown);
    }

    /**
     * Test: isKnownException debe retornar true para IllegalArgumentException.
     */
    @Test
    public void isKnownException_shouldReturnTrueForIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid");

        // Act
        boolean isKnown = GlobalExceptionHandler.isKnownException(exception);

        // Assert
        assertTrue(isKnown);
    }

    /**
     * Test: isKnownException debe retornar true para RuntimeException.
     */
    @Test
    public void isKnownException_shouldReturnTrueForRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Error");

        // Act
        boolean isKnown = GlobalExceptionHandler.isKnownException(exception);

        // Assert
        assertTrue(isKnown);
    }

    /**
     * Test: GlobalExceptionHandler con MainController para integración.
     */
    @Test
    public void handle_shouldWorkWithMainControllerException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Default name not found in repository");

        // Act
        ApiError error = GlobalExceptionHandler.handle(exception);

        // Assert
        assertNotNull(error);
        assertEquals("Default name not found in repository", error.getMessage());
        assertEquals(404, error.getCode());
    }
}


package es.iesquevedo.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite de tests unitarios para ErrorMapper.
 * Verifica el mapeo correcto de excepciones a ApiError.
 */
public class ErrorMapperTest {

    /**
     * Test: NotFoundException debe mapearse a ApiError con código 404.
     */
    @Test
    public void mapException_shouldMapNotFoundExceptionTo404() {
        // Arrange
        NotFoundException exception = new NotFoundException("Resource not found");

        // Act
        ApiError error = ErrorMapper.mapException(exception);

        // Assert
        assertNotNull(error);
        assertEquals("Resource not found", error.getMessage());
        assertEquals(404, error.getCode());
    }

    /**
     * Test: Excepción genérica debe mapearse a ApiError con código 500.
     */
    @Test
    public void mapException_shouldMapGenericExceptionTo500() {
        // Arrange
        Exception exception = new Exception("Something went wrong");

        // Act
        ApiError error = ErrorMapper.mapException(exception);

        // Assert
        assertNotNull(error);
        assertEquals(500, error.getCode());
        assertTrue(error.getMessage().contains("Exception"));
    }

    /**
     * Test: notFound() debe crear ApiError con código 404.
     */
    @Test
    public void notFound_shouldCreateApiErrorWith404() {
        // Arrange
        String message = "User not found";

        // Act
        ApiError error = ErrorMapper.notFound(message);

        // Assert
        assertNotNull(error);
        assertEquals(message, error.getMessage());
        assertEquals(404, error.getCode());
    }

    /**
     * Test: badRequest() debe crear ApiError con código 400.
     */
    @Test
    public void badRequest_shouldCreateApiErrorWith400() {
        // Arrange
        String message = "Invalid input";

        // Act
        ApiError error = ErrorMapper.badRequest(message);

        // Assert
        assertNotNull(error);
        assertEquals(message, error.getMessage());
        assertEquals(400, error.getCode());
    }

    /**
     * Test: internalServerError() debe crear ApiError con código 500.
     */
    @Test
    public void internalServerError_shouldCreateApiErrorWith500() {
        // Arrange
        String message = "Database connection failed";

        // Act
        ApiError error = ErrorMapper.internalServerError(message);

        // Assert
        assertNotNull(error);
        assertEquals(message, error.getMessage());
        assertEquals(500, error.getCode());
    }

    /**
     * Test: Verificar que los códigos HTTP son correctos para diferentes escenarios.
     */
    @Test
    public void mapException_shouldReturnCorrectHttpCodes() {
        // Arrange & Act & Assert
        ApiError notFoundError = ErrorMapper.mapException(
            new NotFoundException("Not found")
        );
        assertEquals(404, notFoundError.getCode());

        ApiError badRequestError = ErrorMapper.badRequest("Bad request");
        assertEquals(400, badRequestError.getCode());

        ApiError serverError = ErrorMapper.internalServerError("Server error");
        assertEquals(500, serverError.getCode());
    }

    /**
     * Test: ApiError debe retornar mensaje y código correctos.
     */
    @Test
    public void apiError_shouldReturnMessageAndCode() {
        // Arrange
        String message = "Test error";
        int code = 400;

        // Act
        ApiError error = new ApiError(message, code);

        // Assert
        assertEquals(message, error.getMessage());
        assertEquals(code, error.getCode());
    }

    /**
     * Test: Mapeo de NotFoundException con diferentes mensajes.
     */
    @Test
    public void mapException_shouldPreserveExceptionMessage() {
        // Arrange
        String message = "Default name not found in repository";
        NotFoundException exception = new NotFoundException(message);

        // Act
        ApiError error = ErrorMapper.mapException(exception);

        // Assert
        assertEquals(message, error.getMessage());
        assertEquals(404, error.getCode());
    }
}

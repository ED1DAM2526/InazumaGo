package es.iesquevedo.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExceptionHandlerTest {

    private ExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new ExceptionHandler();
    }

    @Test
    @DisplayName("Debe mapear NotFoundException a ApiError con código 404")
    void shouldMapNotFoundExceptionToApiErrorWith404() {
        // GIVEN
        String errorMessage = "Recurso no encontrado";
        NotFoundException exception = new NotFoundException(errorMessage);

        // WHEN
        ApiError response = exceptionHandler.handle(exception);

        // THEN
        assertNotNull(response, "La respuesta no debería ser nula");
        assertEquals(404, response.getCode(), "El código de error debe ser 404");
        assertEquals(errorMessage, response.getMessage(), "El mensaje de error debe coincidir");
    }

    @Test
    @DisplayName("Debe mapear otras RuntimeException a ApiError con código 500")
    void shouldMapOtherExceptionsTo500() {
        // GIVEN
        RuntimeException exception = new RuntimeException("Error inesperado");

        // WHEN
        ApiError response = exceptionHandler.handle(exception);

        // THEN
        assertEquals(500, response.getCode());
        assertEquals("Internal Server Error", response.getMessage());
    }
}

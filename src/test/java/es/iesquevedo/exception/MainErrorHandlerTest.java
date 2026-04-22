package es.iesquevedo.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainErrorHandlerTest {

    @Test
    void toApiError_shouldMapNotFoundTo404() {
        MainErrorHandler handler = new MainErrorHandler();

        ApiError result = handler.toApiError(new NotFoundException("Resource not found"));

        assertEquals(404, result.getCode());
        assertEquals("Resource not found", result.getMessage());
    }

    @Test
    void toApiError_shouldMapGenericRuntimeExceptionTo500() {
        MainErrorHandler handler = new MainErrorHandler();

        ApiError result = handler.toApiError(new RuntimeException("boom"));

        assertEquals(500, result.getCode());
        assertEquals("Internal error", result.getMessage());
    }
}

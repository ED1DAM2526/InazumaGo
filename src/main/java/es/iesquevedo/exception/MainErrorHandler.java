package es.iesquevedo.exception;

/**
 * Mapea excepciones de la aplicación a un formato de error estándar.
 */
public class MainErrorHandler {

    public ApiError toApiError(RuntimeException exception) {
        if (exception instanceof NotFoundException) {
            return new ApiError(exception.getMessage(), 404);
        }
        return new ApiError("Internal error", 500);
    }
}

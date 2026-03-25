package es.iesquevedo.exception;

/**
 * Mapea excepciones a respuestas de error estandarizadas (ApiError).
 * 
 * Propósito: Centralizar la conversión de excepciones de la aplicación
 * a objetos ApiError que pueden ser retornados a la UI o clientes.
 * 
 * Ejemplo:
 *   try {
 *       service.greet();
 *   } catch (NotFoundException e) {
 *       ApiError error = ErrorMapper.mapException(e);
 *       // Retornar error al usuario
 *   }
 */
public class ErrorMapper {

    /**
     * Mapea una excepción a un ApiError.
     * 
     * @param exception la excepción a mapear
     * @return ApiError con código de error y mensaje
     */
    public static ApiError mapException(Exception exception) {
        if (exception instanceof NotFoundException) {
            return mapNotFoundException((NotFoundException) exception);
        }
        
        // Caso por defecto: error genérico
        return new ApiError("Internal server error: " + exception.getClass().getSimpleName(), 500);
    }

    /**
     * Mapea NotFoundException a ApiError con código 404.
     * 
     * @param exception la NotFoundException
     * @return ApiError con código 404
     */
    private static ApiError mapNotFoundException(NotFoundException exception) {
        return new ApiError(exception.getMessage(), 404);
    }

    /**
     * Crea un ApiError para recursos no encontrados.
     * Método auxiliar para crear errores 404 sin lanzar excepción.
     * 
     * @param message mensaje de error descriptivo
     * @return ApiError con código 404
     */
    public static ApiError notFound(String message) {
        return new ApiError(message, 404);
    }

    /**
     * Crea un ApiError para errores de validación.
     * 
     * @param message mensaje de error descriptivo
     * @return ApiError con código 400
     */
    public static ApiError badRequest(String message) {
        return new ApiError(message, 400);
    }

    /**
     * Crea un ApiError para errores internos del servidor.
     * 
     * @param message mensaje de error descriptivo
     * @return ApiError con código 500
     */
    public static ApiError internalServerError(String message) {
        return new ApiError(message, 500);
    }
}


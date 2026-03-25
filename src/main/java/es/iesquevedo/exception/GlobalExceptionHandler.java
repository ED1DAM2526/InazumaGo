package es.iesquevedo.exception;

/**
 * Manejador global de excepciones.
 * 
 * Responsabilidad: Interceptar excepciones no manejadas en toda la aplicación
 * y transformarlas a ApiError de forma centralizada.
 * 
 * Propósito: Evitar que cada controlador tenga que manejar excepciones manualmente.
 * Proporciona un punto único de transformación de excepciones a respuestas de error.
 * 
 * Características:
 * - Maneja NotFoundException → ApiError(message, 404)
 * - Maneja IllegalArgumentException → ApiError(message, 400)
 * - Maneja RuntimeException → ApiError(message, 500)
 * - Métodos auxiliares para obtener códigos HTTP
 * - Validación de excepciones conocidas
 * 
 * Uso:
 *   try {
 *       controller.greet();
 *   } catch (Exception e) {
 *       ApiError error = GlobalExceptionHandler.handle(e);
 *       // Retornar error al usuario
 *   }
 */
public class GlobalExceptionHandler {

    /**
     * Maneja una excepción transformándola a ApiError.
     * 
     * Este es el método principal que centraliza todo el manejo de excepciones.
     * Discrimina por tipo de excepción y retorna el código HTTP apropiado.
     * 
     * @param exception la excepción a manejar
     * @return ApiError con código HTTP y mensaje descriptivo
     */
    public static ApiError handle(Exception exception) {
        if (exception instanceof NotFoundException) {
            return handleNotFound((NotFoundException) exception);
        }
        
        if (exception instanceof IllegalArgumentException) {
            return handleBadRequest((IllegalArgumentException) exception);
        }
        
        if (exception instanceof RuntimeException) {
            return handleServerError((RuntimeException) exception);
        }
        
        // Por defecto: error interno del servidor para excepciones desconocidas
        return ErrorMapper.internalServerError(
            "Unexpected error: " + exception.getClass().getSimpleName()
        );
    }

    /**
     * Maneja NotFoundException.
     * Retorna error 404 con mensaje descriptivo.
     * 
     * @param exception la excepción NotFoundException
     * @return ApiError con código 404
     */
    private static ApiError handleNotFound(NotFoundException exception) {
        return ErrorMapper.mapException(exception);
    }

    /**
     * Maneja IllegalArgumentException.
     * Retorna error 400 (Bad Request).
     * 
     * @param exception la excepción IllegalArgumentException
     * @return ApiError con código 400
     */
    private static ApiError handleBadRequest(IllegalArgumentException exception) {
        return ErrorMapper.badRequest(
            "Invalid argument: " + exception.getMessage()
        );
    }

    /**
     * Maneja RuntimeException genérica.
     * Retorna error 500 (Internal Server Error).
     * 
     * @param exception la excepción RuntimeException
     * @return ApiError con código 500
     */
    private static ApiError handleServerError(RuntimeException exception) {
        return ErrorMapper.internalServerError(
            "Internal error: " + exception.getClass().getSimpleName()
        );
    }

    /**
     * Valida si una excepción es conocida (manejable por este handler).
     * 
     * @param exception la excepción a validar
     * @return true si es una excepción conocida, false si es inesperada
     */
    public static boolean isKnownException(Exception exception) {
        return exception instanceof RuntimeException;
    }

    /**
     * Obtiene el código HTTP apropiado para una excepción.
     * 
     * Mapeo:
     * - NotFoundException → 404
     * - IllegalArgumentException → 400
     * - Otros → 500
     * 
     * @param exception la excepción
     * @return código HTTP (400, 404, 500)
     */
    public static int getHttpStatusCode(Exception exception) {
        if (exception instanceof NotFoundException) {
            return 404;
        }
        if (exception instanceof IllegalArgumentException) {
            return 400;
        }
        return 500;
    }
}

package es.iesquevedo.exception;

public class ExceptionHandler {

    public ApiError handle(RuntimeException exception) {
        if (exception instanceof NotFoundException) {
            return new ApiError(exception.getMessage(), 404);
        }
        return new ApiError("Internal Server Error", 500);
    }
}

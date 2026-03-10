package es.iesquevedo.exception;

public class ApiError {
    private final String message;
    private final int code;

    public ApiError(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}


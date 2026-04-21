package es.iesquevedo.service.auth;

/**
 * Interfaz para gestión de autenticación y tokens.
 * Responsable de login y gestión de tokens para llamadas autenticadas a Firebase.
 */
public interface AuthService {
    /**
     * Realiza login con email y contraseña.
     *
     * @param email correo del usuario
     * @param password contraseña del usuario
     * @return token de autenticación
     * @throws Exception si el login falla
     */
    String login(String email, String password) throws Exception;

    /**
     * Obtiene el token actual.
     *
     * @return token guardado en memoria, o null si no hay autenticación
     */
    String getToken();

    /**
     * Cierra la sesión y limpia el token.
     */
    void logout();

    /**
     * Verifica si la sesión está activa.
     *
     * @return true si hay token válido
     */
    boolean isAuthenticated();
}


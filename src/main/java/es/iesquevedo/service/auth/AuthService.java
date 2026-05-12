package es.iesquevedo.service.auth;

public interface AuthService {
    /**
     * Autentica un usuario con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return Token de autenticación si es exitoso, null en caso contrario
     */
    String login(String email, String password);

    /**
     * Registra un nuevo usuario con email y contraseña.
     * @param email Email del nuevo usuario
     * @param password Contraseña del nuevo usuario
     * @return Token de autenticación si es exitoso, null en caso contrario
     */
    String register(String email, String password);

    /**
     * Obtiene el token de autenticación actual.
     * @return Token actual o null si no hay sesión activa
     */
    String getToken();

    /**
     * Cierra la sesión actual.
     */
    void logout();
}
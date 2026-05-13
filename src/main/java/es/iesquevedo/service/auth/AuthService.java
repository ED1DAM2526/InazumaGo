package es.iesquevedo.service.auth;

import java.util.Optional;

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
     * @return Optional con el token guardado en memoria, o vacío si no hay autenticación
     */
    Optional<String> getToken();

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

    /**
     * Registra un nuevo usuario.
     *
     * @param email correo del usuario
     * @param password contraseña del usuario
     * @return token de autenticación si el registro es exitoso, null si falla
     * @throws Exception si el registro falla
     */
    String register(String email, String password) throws Exception;
}

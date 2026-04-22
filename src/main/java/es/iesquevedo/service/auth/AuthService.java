package es.iesquevedo.service.auth;

 E3-US2-T4
import java.util.Optional;

/**
 * Contrato minimo de autenticacion para el cliente.
 */
public interface AuthService {
    String login(String email, String password);

    Optional<String> getToken();

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
 dev
}


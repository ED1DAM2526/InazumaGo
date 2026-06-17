package es.iesquevedo.service;

import java.util.concurrent.CompletableFuture;

/**
 * Servicio de autenticación para obtener tokens de Firebase.
 */
public interface AuthService {
    
    /**
     * Login con email y contraseña.
     * 
     * @param email correo del usuario
     * @param password contraseña
     * @return CompletableFuture con token de autenticación
     */
    CompletableFuture<String> login(String email, String password);
    
    /**
     * Obtiene el token actual (si existe y no ha expirado).
     */
    String getCurrentToken();
    
    /**
     * Logout y limpia token.
     */
    void logout();
}

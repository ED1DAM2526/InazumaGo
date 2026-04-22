package es.iesquevedo.service.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación mock de AuthService para pruebas y desarrollo.
 * Devuelve un token fijo y permite inyección de tokens en tests.
 */
public class AuthServiceMock implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceMock.class.getName());
    
    private String currentToken;
    private static final String MOCK_TOKEN_PREFIX = "mock_token_";

    public AuthServiceMock() {
        this.currentToken = null;
    }

    /**
     * Login simulado que devuelve un token de desarrollo.
     * En desarrollo, acepta cualquier email/password.
     * 
     * @param email correo del usuario
     * @param password contraseña del usuario
     * @return token de desarrollo
     * @throws Exception si email o password están vacíos
     */
    @Override
    public String login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email no puede estar vacío");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Contraseña no puede estar vacía");
        }

        // En desarrollo: generar token simulado
        this.currentToken = MOCK_TOKEN_PREFIX + System.currentTimeMillis();
        LOGGER.log(Level.INFO, "Login exitoso para: " + email);
        LOGGER.log(Level.INFO, "Token generado: " + this.currentToken);
        
        return this.currentToken;
    }

    @Override
    public String getToken() {
        return this.currentToken;
    }

    @Override
    public void logout() {
        LOGGER.log(Level.INFO, "Logout ejecutado");
        this.currentToken = null;
    }

    @Override
    public boolean isAuthenticated() {
        return this.currentToken != null && !this.currentToken.isEmpty();
    }

    /**
     * Método auxiliar para tests: inyectar token directamente.
     * 
     * @param token token a inyectar
     */
    public void injectToken(String token) {
        this.currentToken = token;
        LOGGER.log(Level.INFO, "Token inyectado: " + token);
    }

    /**
     * Método auxiliar para tests: simular expiración de token.
     */
    public void simulateExpiration() {
        LOGGER.log(Level.INFO, "Token expirado simulado");
        this.currentToken = null;
    }
}


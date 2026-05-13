package es.iesquevedo.service.impl;

import es.iesquevedo.service.AuthService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación mock de AuthService para desarrollo.
 * En producción, se usaría Firebase Authentication real.
 */
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    private String currentToken;
    private String currentEmail;

    @Override
    public CompletableFuture<String> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            // Validaciones mínimas (en prod, verificar contra Firebase Auth)
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email no puede estar vacío");
            }
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
            }
            
            // Generar token mock (en prod sería de Firebase)
            this.currentToken = "dev-token-" + UUID.randomUUID();
            this.currentEmail = email;
            
            LOGGER.log(Level.INFO, "Login exitoso: " + email);
            return this.currentToken;
        });
    }

    @Override
    public String getCurrentToken() {
        return currentToken;
    }

    @Override
    public void logout() {
        this.currentToken = null;
        this.currentEmail = null;
        LOGGER.log(Level.INFO, "Logout realizado");
    }
}

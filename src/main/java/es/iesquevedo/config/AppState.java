package es.iesquevedo.config;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton para almacenar estado global de la aplicación.
 * Gestiona el token de autenticación, refresh token y datos de sesión.
 */
public class AppState {
    private static final Logger LOGGER = Logger.getLogger(AppState.class.getName());
    private static final AppState INSTANCE = new AppState();
    
    private String authToken;
    private String refreshToken;
    private String currentUserId;
    private String currentUserEmail;
    private long tokenExpirationTime;

    /**
     * Constructor privado para Singleton.
     */
    private AppState() {
        this.authToken = null;
        this.refreshToken = null;
        this.currentUserId = null;
        this.currentUserEmail = null;
        this.tokenExpirationTime = 0;
    }

    /**
     * Obtiene la instancia única de AppState.
     * 
     * @return instancia de AppState
     */
    public static AppState getInstance() {
        return INSTANCE;
    }

    /**
     * Guarda el token de autenticación en memoria.
     * 
     * @param token token a guardar
     */
    public void setAuthToken(String token) {
        this.authToken = token;
        LOGGER.log(Level.INFO, "Token guardado en AppState");
    }

    /**
     * Obtiene el token de autenticación actual.
     * 
     * @return token o null si no hay autenticación
     */
    public String getAuthToken() {
        return this.authToken;
    }

    /**
     * Guarda el email del usuario autenticado.
     * 
     * @param email email del usuario
     */
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        LOGGER.log(Level.INFO, "Email del usuario guardado: " + email);
    }

    /**
     * Obtiene el email del usuario autenticado.
     * 
     * @return email del usuario o null
     */
    public String getCurrentUserEmail() {
        return this.currentUserEmail;
    }

    /**
     * Verifica si hay una sesión activa.
     * 
     * @return true si hay token guardado y no está expirado
     */
    public boolean isAuthenticated() {
        if (this.authToken == null || this.authToken.isEmpty()) {
            return false;
        }
        // Si tokenExpirationTime es 0 (no configurado), considera que está válido
        if (this.tokenExpirationTime == 0) {
            return true;
        }
        // Si está configurado, verificar que no esté expirado
        return System.currentTimeMillis() < this.tokenExpirationTime;
    }

    /**
     * Guarda el refresh token (para renovación de sesión).
     * 
     * @param refreshToken token de refresco
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        LOGGER.log(Level.INFO, "Refresh token almacenado");
    }

    /**
     * Obtiene el refresh token.
     * 
     * @return refresh token o null
     */
    public String getRefreshToken() {
        return this.refreshToken;
    }

    /**
     * Guarda el ID del usuario autenticado.
     * 
     * @param userId ID del usuario
     */
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
        LOGGER.log(Level.INFO, "ID de usuario guardado");
    }

    /**
     * Obtiene el ID del usuario autenticado.
     * 
     * @return ID del usuario o null
     */
    public String getCurrentUserId() {
        return this.currentUserId;
    }

    /**
     * Guarda el tiempo de expiración del token.
     * 
     * @param expirationTime tiempo en milisegundos desde epoch
     */
    public void setTokenExpirationTime(long expirationTime) {
        this.tokenExpirationTime = expirationTime;
    }

    /**
     * Verifica si el token está próximo a expirar.
     * 
     * @return true si el token expira en menos de 5 minutos
     */
    public boolean isTokenExpiring() {
        long timeUntilExpiry = tokenExpirationTime - System.currentTimeMillis();
        return timeUntilExpiry < (5 * 60 * 1000); // 5 minutos
    }

    /**
     * Limpia el estado de autenticación (logout).
     */
    public void clear() {
        this.authToken = null;
        this.refreshToken = null;
        this.currentUserId = null;
        this.currentUserEmail = null;
        this.tokenExpirationTime = 0;
        LOGGER.log(Level.INFO, "AppState limpiado (logout)");
    }
}


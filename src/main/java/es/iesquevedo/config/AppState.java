package es.iesquevedo.config;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton para almacenar estado global de la aplicación.
 * Gestiona el token de autenticación durante la sesión.
 */
public class AppState {
    private static final Logger LOGGER = Logger.getLogger(AppState.class.getName());
    private static final AppState INSTANCE = new AppState();
    
    private String authToken;
    private String currentUserEmail;

    /**
     * Constructor privado para Singleton.
     */
    private AppState() {
        this.authToken = null;
        this.currentUserEmail = null;
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
     * @return true si hay token guardado
     */
    public boolean isAuthenticated() {
        return this.authToken != null && !this.authToken.isEmpty();
    }

    /**
     * Limpia el estado de autenticación (logout).
     */
    public void clear() {
        this.authToken = null;
        this.currentUserEmail = null;
        LOGGER.log(Level.INFO, "AppState limpiado (logout)");
    }
}


package es.iesquevedo.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.iesquevedo.service.AuthService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación de AuthService con Firebase Authentication REST API.
 * Usa los endpoints de Google Identity Toolkit para signup/signin.
 * Maneja tokens de acceso, refresh tokens y refresco automático.
 */
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    
    // Firebase Web API Key
    private static final String API_KEY = "AIzaSyAiRDDRO6MJjuMgbQ28v6CvtaF2qx_ZVIk";
    
    // Firebase Authentication REST endpoints
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
    private static final String REFRESH_TOKEN_URL = "https://securetoken.googleapis.com/v1/token?key=" + API_KEY;
    
    private static final String CONTENT_TYPE = "application/json";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String currentToken;
    private String refreshToken;
    private String currentEmail;
    private String currentUserId;
    private long tokenExpirationTime;

    public AuthServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.tokenExpirationTime = 0;
    }

    /**
     * Registrar un nuevo usuario en Firebase Authentication
     */
    public CompletableFuture<String> signup(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email no puede estar vacío");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Contraseña no puede estar vacía");
            }
            if (password.length() < 6) {
                throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
            }
            if (!email.contains("@")) {
                throw new IllegalArgumentException("Email inválido (debe contener @)");
            }

            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("email", email);
                requestBody.addProperty("password", password);
                requestBody.addProperty("returnSecureToken", true);

                Request request = new Request.Builder()
                        .url(SIGN_UP_URL)
                        .post(RequestBody.create(gson.toJson(requestBody), 
                                okhttp3.MediaType.parse(CONTENT_TYPE)))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        LOGGER.log(Level.WARNING, "Signup failed: " + errorBody);
                        
                        // Parsear error message de Firebase
                        String errorMessage = parseFirebaseError(errorBody);
                        throw new RuntimeException(errorMessage);
                    }

                    JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                    this.currentToken = responseBody.get("idToken").getAsString();
                    this.refreshToken = responseBody.get("refreshToken").getAsString();
                    this.currentEmail = email;
                    this.currentUserId = responseBody.get("localId").getAsString();
                    this.tokenExpirationTime = System.currentTimeMillis() + (3600 * 1000); // 1 hora
                    
                    LOGGER.log(Level.INFO, "Signup exitoso: " + email);
                    return this.currentToken;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Signup error: " + e.getMessage());
                throw new RuntimeException("Error en signup: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Parsea errores de Firebase y devuelve mensaje legible
     */
    private String parseFirebaseError(String errorBody) {
        try {
            JsonObject error = gson.fromJson(errorBody, JsonObject.class);
            if (error.has("error")) {
                JsonObject errorDetails = error.getAsJsonObject("error");
                if (errorDetails.has("message")) {
                    String message = errorDetails.get("message").getAsString();
                    // Traducir mensajes de Firebase
                    if (message.contains("EMAIL_EXISTS")) {
                        return "Este email ya está registrado";
                    } else if (message.contains("INVALID_EMAIL")) {
                        return "Email inválido";
                    } else if (message.contains("WEAK_PASSWORD")) {
                        return "Contraseña muy débil (mín. 6 caracteres)";
                    } else if (message.contains("OPERATION_NOT_ALLOWED")) {
                        return "Registro deshabilitado en Firebase";
                    }
                    return "Error: " + message;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parseando error Firebase: " + e.getMessage());
        }
        return "Error en signup. Verifica email y contraseña";
    }

    @Override
    public CompletableFuture<String> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email no puede estar vacío");
            }
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Contraseña no puede estar vacía");
            }

            try {
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("email", email);
                requestBody.addProperty("password", password);
                requestBody.addProperty("returnSecureToken", true);

                Request request = new Request.Builder()
                        .url(SIGN_IN_URL)
                        .post(RequestBody.create(gson.toJson(requestBody), 
                                okhttp3.MediaType.parse(CONTENT_TYPE)))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        LOGGER.log(Level.WARNING, "Login failed: " + errorBody);
                        
                        String errorMessage = parseLoginError(errorBody);
                        throw new RuntimeException(errorMessage);
                    }

                    JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                    this.currentToken = responseBody.get("idToken").getAsString();
                    this.refreshToken = responseBody.get("refreshToken").getAsString();
                    this.currentEmail = email;
                    this.currentUserId = responseBody.get("localId").getAsString();
                    this.tokenExpirationTime = System.currentTimeMillis() + (3600 * 1000); // 1 hora
                    
                    LOGGER.log(Level.INFO, "Login exitoso: " + email);
                    return this.currentToken;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Login error: " + e.getMessage());
                throw new RuntimeException("Error en login: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Parsea errores de login de Firebase
     */
    private String parseLoginError(String errorBody) {
        try {
            JsonObject error = gson.fromJson(errorBody, JsonObject.class);
            if (error.has("error")) {
                JsonObject errorDetails = error.getAsJsonObject("error");
                if (errorDetails.has("message")) {
                    String message = errorDetails.get("message").getAsString();
                    if (message.contains("INVALID_LOGIN_CREDENTIALS")) {
                        return "Email o contraseña incorrectos";
                    } else if (message.contains("USER_DISABLED")) {
                        return "Usuario deshabilitado";
                    } else if (message.contains("INVALID_EMAIL")) {
                        return "Email inválido";
                    }
                    return "Error: " + message;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parseando error login Firebase: " + e.getMessage());
        }
        return "Error en login. Verifica email y contraseña";
    }

    @Override
    public String getCurrentToken() {
        // Si el token está próximo a expirar, intentar refrescarlo
        if (isTokenExpiring()) {
            try {
                refreshAccessToken();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo refrescar token: " + e.getMessage());
            }
        }
        return currentToken;
    }

    /**
     * Refresca el token de acceso usando el refresh token.
     * Se llama automáticamente si el token está próximo a expirar.
     */
    public String refreshAccessToken() {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalStateException("Refresh token no disponible");
        }

        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("grant_type", "refresh_token");
            requestBody.addProperty("refresh_token", refreshToken);

            Request request = new Request.Builder()
                    .url(REFRESH_TOKEN_URL)
                    .post(RequestBody.create(gson.toJson(requestBody), 
                            okhttp3.MediaType.parse(CONTENT_TYPE)))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    LOGGER.log(Level.WARNING, "Token refresh fallido: " + response.code());
                    throw new RuntimeException("Token refresh failed: HTTP " + response.code());
                }

                JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                this.currentToken = responseBody.get("id_token").getAsString();
                this.tokenExpirationTime = System.currentTimeMillis() + (3600 * 1000); // 1 hora
                
                LOGGER.log(Level.INFO, "Token refrescado exitosamente");
                return this.currentToken;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error en token refresh: " + e.getMessage());
            throw new RuntimeException("Error en token refresh: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el token está por expirar (próximos 5 minutos).
     */
    private boolean isTokenExpiring() {
        long timeUntilExpiry = tokenExpirationTime - System.currentTimeMillis();
        return timeUntilExpiry < (5 * 60 * 1000); // 5 minutos
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public void logout() {
        this.currentToken = null;
        this.refreshToken = null;
        this.currentEmail = null;
        this.currentUserId = null;
        this.tokenExpirationTime = 0;
        LOGGER.log(Level.INFO, "Logout realizado");
    }
}

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
 */
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthServiceImpl.class.getName());
    
    // Firebase Web API Key
    private static final String API_KEY = "AIzaSyAiRDDRO6MJjuMgbQ28v6CvtaF2qx_ZVIk";
    
    // Firebase Authentication REST endpoints
    private static final String SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=" + API_KEY;
    private static final String SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
    
    private static final String CONTENT_TYPE = "application/json";
    
    private final OkHttpClient httpClient;
    private final Gson gson;
    private String currentToken;
    private String currentEmail;
    private String currentUserId;

    public AuthServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Registrar un nuevo usuario en Firebase Authentication
     */
    public CompletableFuture<String> signup(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email no puede estar vacío");
            }
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
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
                        throw new RuntimeException("Signup failed: HTTP " + response.code());
                    }

                    JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                    this.currentToken = responseBody.get("idToken").getAsString();
                    this.currentEmail = email;
                    this.currentUserId = responseBody.get("localId").getAsString();
                    
                    LOGGER.log(Level.INFO, "Signup exitoso: " + email);
                    return this.currentToken;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Signup error: " + e.getMessage());
                throw new RuntimeException("Error en signup: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public CompletableFuture<String> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email no puede estar vacío");
            }
            if (password == null || password.length() < 6) {
                throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
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
                        throw new RuntimeException("Login failed: credenciales inválidas");
                    }

                    JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                    this.currentToken = responseBody.get("idToken").getAsString();
                    this.currentEmail = email;
                    this.currentUserId = responseBody.get("localId").getAsString();
                    
                    LOGGER.log(Level.INFO, "Login exitoso: " + email);
                    return this.currentToken;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Login error: " + e.getMessage());
                throw new RuntimeException("Error en login: " + e.getMessage(), e);
            }
        });
    }

    @Override
    public String getCurrentToken() {
        return currentToken;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    @Override
    public void logout() {
        this.currentToken = null;
        this.currentEmail = null;
        this.currentUserId = null;
        LOGGER.log(Level.INFO, "Logout realizado");
    }
}

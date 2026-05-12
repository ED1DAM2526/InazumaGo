package es.iesquevedo.service.auth.impl;

import es.iesquevedo.service.auth.AuthService;
import java.util.HashMap;
import java.util.Map;

public class AuthServiceMock implements AuthService {

    private String currentToken;
    private Map<String, String> registeredUsers = new HashMap<>();

    public AuthServiceMock() {
        // Usuarios de prueba predefinidos
        registeredUsers.put("test@test.com", "password123");
        registeredUsers.put("demo@demo.com", "demo123");
    }

    @Override
    public String login(String email, String password) {
        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            // Verificar si el usuario existe y la contraseña es correcta
            if (registeredUsers.containsKey(email) && registeredUsers.get(email).equals(password)) {
                currentToken = "mock-token-" + System.currentTimeMillis();
                return currentToken;
            }
        }
        return null;
    }

    @Override
    public String register(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        
        // Verificar si el email ya está registrado
        if (registeredUsers.containsKey(email)) {
            return null; // El email ya existe
        }
        
        // Registrar el nuevo usuario
        registeredUsers.put(email, password);
        currentToken = "mock-token-" + System.currentTimeMillis();
        return currentToken;
    }

    @Override
    public String getToken() {
        return currentToken;
    }

    @Override
    public void logout() {
        currentToken = null;
    }
}
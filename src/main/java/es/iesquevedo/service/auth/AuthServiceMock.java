package es.iesquevedo.service.auth;

import java.util.Optional;

/**
 * Implementacion de desarrollo para simular login sin dependencia externa.
 */
public class AuthServiceMock implements AuthService {
    private String token;

    @Override
    public String login(String email, String password) {
        this.token = "dev-token-" + email;
        return this.token;
    }

    @Override
    public Optional<String> getToken() {
        return Optional.ofNullable(token);
    }
}


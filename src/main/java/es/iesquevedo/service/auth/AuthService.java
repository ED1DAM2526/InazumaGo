package es.iesquevedo.service.auth;

import java.util.Optional;

/**
 * Contrato minimo de autenticacion para el cliente.
 */
public interface AuthService {
    String login(String email, String password);

    Optional<String> getToken();
}


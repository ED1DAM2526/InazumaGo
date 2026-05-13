package es.iesquevedo.service;

import es.iesquevedo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para AuthService (mock para desarrollo).
 */
public class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl();
    }

    @Test
    void testLoginSuccess() throws Exception {
        CompletableFuture<String> result = authService.login("test@example.com", "password123");
        String token = result.get();

        assertNotNull(token);
        assertTrue(token.startsWith("dev-token-"));
        assertEquals(token, authService.getCurrentToken());
    }

    @Test
    void testLoginWithInvalidEmail() {
        assertThrows(Exception.class, () -> 
            authService.login("", "password123").get()
        );
    }

    @Test
    void testLoginWithShortPassword() {
        assertThrows(Exception.class, () -> 
            authService.login("test@example.com", "12345").get()
        );
    }

    @Test
    void testLogout() throws Exception {
        authService.login("test@example.com", "password123").get();
        assertNotNull(authService.getCurrentToken());

        authService.logout();
        assertNull(authService.getCurrentToken());
    }
}

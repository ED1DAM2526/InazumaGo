package es.iesquevedo.controller;

import es.iesquevedo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para RegisterController.
 * Verifica que el registro con Firebase Auth funciona correctamente.
 */
public class RegisterControllerTest {

    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthServiceImpl();
    }

    @Test
    public void testSignupWithValidCredentials() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@test.com";
        String password = "password123";

        CompletableFuture<String> result = authService.signup(email, password);
        String token = result.get();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(email, authService.getCurrentEmail());
    }

    @Test
    public void testSignupWithShortPassword() {
        String email = "test@test.com";
        String password = "12345";

        assertThrows(ExecutionException.class, () -> {
            authService.signup(email, password).get();
        });
    }

    @Test
    public void testSignupWithEmptyEmail() {
        String email = "";
        String password = "password123";

        assertThrows(ExecutionException.class, () -> {
            authService.signup(email, password).get();
        });
    }

    @Test
    public void testSignupAndGetUserId() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@test.com";
        String password = "password123";

        CompletableFuture<String> result = authService.signup(email, password);
        String token = result.get();

        assertNotNull(authService.getCurrentUserId());
        assertFalse(authService.getCurrentUserId().isBlank());
    }

    @Test
    public void testLogoutAfterSignup() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@test.com";
        String password = "password123";

        authService.signup(email, password).get();
        assertNotNull(authService.getCurrentToken());

        authService.logout();
        assertNull(authService.getCurrentToken());
        assertNull(authService.getCurrentEmail());
    }
}



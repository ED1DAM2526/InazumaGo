package es.iesquevedo.service;

import es.iesquevedo.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para AuthService con Firebase Auth real.
 */
public class AuthServiceTest {
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl();
    }

    @Test
    void testSignupSuccess() throws Exception {
        String email = "signup" + System.currentTimeMillis() + "@test.com";
        String password = "password123";
        
        if (authService instanceof AuthServiceImpl) {
            AuthServiceImpl authImpl = (AuthServiceImpl) authService;
            CompletableFuture<String> result = authImpl.signup(email, password);
            String token = result.get();

            assertNotNull(token);
            assertFalse(token.isBlank());
            assertEquals(email, authImpl.getCurrentEmail());
        }
    }

    @Test
    void testLoginWithInvalidEmail() {
        assertThrows(ExecutionException.class, () -> 
            authService.login("", "password123").get()
        );
    }

    @Test
    void testLoginWithShortPassword() {
        assertThrows(ExecutionException.class, () -> 
            authService.login("test@example.com", "12345").get()
        );
    }

    @Test
    void testLogoutAfterSignup() throws Exception {
        if (authService instanceof AuthServiceImpl) {
            AuthServiceImpl authImpl = (AuthServiceImpl) authService;
            String email = "logout" + System.currentTimeMillis() + "@test.com";
            String password = "password123";
            
            authImpl.signup(email, password).get();
            assertNotNull(authService.getCurrentToken());

            authService.logout();
            assertNull(authService.getCurrentToken());
        }
    }
}

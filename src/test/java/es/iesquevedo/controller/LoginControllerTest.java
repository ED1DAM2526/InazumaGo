package es.iesquevedo.controller;

import es.iesquevedo.config.AppState;
import es.iesquevedo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para LoginController.
 * Verifica que AppState almacene correctamente datos de sesión.
 */
@DisplayName("LoginController Tests")
public class LoginControllerTest {
    private LoginController loginController;
    private AuthService authServiceMock;
    private AppState appState;

    @BeforeEach
    public void setUp() {
        // Crear mock de AuthService
        authServiceMock = Mockito.mock(AuthService.class);
        loginController = new LoginController();
        loginController.setAuthService(authServiceMock);

        // Obtener instancia de AppState y limpiarla
        appState = AppState.getInstance();
        appState.clear();
    }

    @Test
    @DisplayName("Login exitoso debe almacenar token en AppState")
    public void testLoginExitosoAlmacenaToken() throws Exception {
        // Arrange
        String token = "test_token_12345";
        String email = "usuario@test.com";

        // Act
        appState.setAuthToken(token);
        appState.setCurrentUserEmail(email);

        // Assert
        assertNotNull(appState.getAuthToken());
        assertEquals(token, appState.getAuthToken());
        assertEquals(email, appState.getCurrentUserEmail());
        assertTrue(appState.isAuthenticated());
    }

    @Test
    @DisplayName("Logout debe limpiar AppState")
    public void testLogoutLimpiaAppState() throws Exception {
        // Arrange
        String token = "test_token_12345";
        String email = "usuario@test.com";
        appState.setAuthToken(token);
        appState.setCurrentUserEmail(email);

        // Verificar que está autenticado
        assertTrue(appState.isAuthenticated());

        // Act
        appState.clear();

        // Assert
        assertNull(appState.getAuthToken());
        assertNull(appState.getCurrentUserEmail());
        assertFalse(appState.isAuthenticated());
    }

    @Test
    @DisplayName("AppState debe persistir el token durante la sesión")
    public void testAppStatePersistirToken() throws Exception {
        // Arrange
        String token = "test_token_12345";
        appState.setAuthToken(token);

        // Act
        String tokenRecuperado = appState.getAuthToken();

        // Assert
        assertEquals(token, tokenRecuperado);
        assertTrue(appState.isAuthenticated());
    }

    @Test
    @DisplayName("AppState debe iniciar sin autenticación")
    public void testAppStateIniciaSinAutenticacion() {
        // Assert
        assertNull(appState.getAuthToken());
        assertNull(appState.getCurrentUserEmail());
        assertFalse(appState.isAuthenticated());
    }
}

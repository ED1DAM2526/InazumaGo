package es.iesquevedo.controller;

import es.iesquevedo.config.AppState;
import es.iesquevedo.service.auth.AuthServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para LoginController.
 * Verifica que el controlador integre correctamente con AuthServiceMock
 * y almacene el token en AppState.
 */
@DisplayName("LoginController Tests")
public class LoginControllerTest {
    private LoginController loginController;
    private AuthServiceMock authServiceMock;
    private AppState appState;

    @BeforeEach
    public void setUp() {
        // Inicializar mocks y controlador
        authServiceMock = new AuthServiceMock();
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
        String email = "usuario@test.com";
        String password = "password123";

        // Act
        String token = authServiceMock.login(email, password);
        appState.setAuthToken(token);
        appState.setCurrentUserEmail(email);

        // Assert
        assertNotNull(appState.getAuthToken());
        assertEquals(token, appState.getAuthToken());
        assertEquals(email, appState.getCurrentUserEmail());
        assertTrue(appState.isAuthenticated());
    }

    @Test
    @DisplayName("Login con email vacío debe lanzar excepción")
    public void testLoginConEmailVacioLanzaExcepcion() {
        // Assert
        assertThrows(Exception.class, () -> {
            authServiceMock.login("", "password123");
        });
    }

    @Test
    @DisplayName("Login con email null debe lanzar excepción")
    public void testLoginConEmailNullLanzaExcepcion() {
        // Assert
        assertThrows(Exception.class, () -> {
            authServiceMock.login(null, "password123");
        });
    }

    @Test
    @DisplayName("Login con contraseña vacía debe lanzar excepción")
    public void testLoginConContraseñaVaciaLanzaExcepcion() {
        // Assert
        assertThrows(Exception.class, () -> {
            authServiceMock.login("usuario@test.com", "");
        });
    }

    @Test
    @DisplayName("Login con contraseña null debe lanzar excepción")
    public void testLoginConContraseñaNullLanzaExcepcion() {
        // Assert
        assertThrows(Exception.class, () -> {
            authServiceMock.login("usuario@test.com", null);
        });
    }

    @Test
    @DisplayName("Logout debe limpiar AppState")
    public void testLogoutLimpiaAppState() throws Exception {
        // Arrange
        String email = "usuario@test.com";
        String password = "password123";
        String token = authServiceMock.login(email, password);
        appState.setAuthToken(token);
        appState.setCurrentUserEmail(email);

        // Verificar que está autenticado
        assertTrue(appState.isAuthenticated());

        // Act
        authServiceMock.logout();
        appState.clear();

        // Assert
        assertNull(appState.getAuthToken());
        assertNull(appState.getCurrentUserEmail());
        assertFalse(appState.isAuthenticated());
    }

    @Test
    @DisplayName("Token debe ser único por login")
    public void testTokenUnicoPorLogin() throws Exception {
        // Arrange
        String email = "usuario@test.com";
        String password = "password123";

        // Act
        String token1 = authServiceMock.login(email, password);
        String token2 = authServiceMock.login(email, password);

        // Assert
        assertNotEquals(token1, token2, "Cada login debe generar un token diferente");
    }

    @Test
    @DisplayName("AppState debe persistir el token durante la sesión")
    public void testAppStatePersistirToken() throws Exception {
        // Arrange
        String email = "usuario@test.com";
        String token = authServiceMock.login(email, "password123");
        appState.setAuthToken(token);

        // Act
        String tokenRecuperado = appState.getAuthToken();

        // Assert
        assertEquals(token, tokenRecuperado);
        assertTrue(appState.isAuthenticated());
    }

    @Test
    @DisplayName("AuthServiceMock debe permitir inyección de token")
    public void testInyeccionDeToken() {
        // Arrange
        String customToken = "custom_token_12345";

        // Act
        authServiceMock.injectToken(customToken);

        // Assert
        assertEquals(customToken, authServiceMock.getToken());
        assertTrue(authServiceMock.isAuthenticated());
    }

    @Test
    @DisplayName("AuthServiceMock debe simular expiración de token")
    public void testSimulacionExpiracionToken() throws Exception {
        // Arrange
        String email = "usuario@test.com";
        authServiceMock.login(email, "password123");
        assertTrue(authServiceMock.isAuthenticated());

        // Act
        authServiceMock.simulateExpiration();

        // Assert
        assertNull(authServiceMock.getToken());
        assertFalse(authServiceMock.isAuthenticated());
    }

    @Test
    @DisplayName("Múltiples logins deben reemplazar el token anterior")
    public void testMultiplesLoginReemplazanToken() throws Exception {
        // Arrange
        String email1 = "usuario1@test.com";
        String email2 = "usuario2@test.com";

        // Act
        String token1 = authServiceMock.login(email1, "password123");
        appState.setAuthToken(token1);
        appState.setCurrentUserEmail(email1);

        String token2 = authServiceMock.login(email2, "password456");
        appState.setAuthToken(token2);
        appState.setCurrentUserEmail(email2);

        // Assert
        assertEquals(token2, appState.getAuthToken());
        assertEquals(email2, appState.getCurrentUserEmail());
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("AppState debe iniciar sin autenticación")
    public void testAppStateIniciaSinAutenticacion() {
        // Assert
        assertNull(appState.getAuthToken());
        assertNull(appState.getCurrentUserEmail());
        assertFalse(appState.isAuthenticated());
    }

    @Test
    @DisplayName("AuthServiceMock debe generar token con prefijo correcto")
    public void testTokenGeneradoConPrefijoCorrecto() throws Exception {
        // Act
        String token = authServiceMock.login("usuario@test.com", "password123");

        // Assert
        assertTrue(token.startsWith("mock_token_"),
            "El token debe empezar con 'mock_token_'");
        assertNotNull(token);
        assertTrue(token.length() > "mock_token_".length());
    }
}


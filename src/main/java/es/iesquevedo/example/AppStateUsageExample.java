package es.iesquevedo.example;

import es.iesquevedo.config.AppState;

/**
 * EJEMPLO: Cómo acceder al token después de login
 * Este archivo es solo referencia, no forma parte del código principal.
 */
public class AppStateUsageExample {

    /**
     * Ejemplo 1: Verificar si el usuario está autenticado
     */
    public static void checkAuthentication() {
        AppState appState = AppState.getInstance();
        
        if (appState.isAuthenticated()) {
            System.out.println("Usuario autenticado");
            System.out.println("Email: " + appState.getCurrentUserEmail());
        } else {
            System.out.println("No hay usuario autenticado");
        }
    }

    /**
     * Ejemplo 2: Obtener el token para una llamada HTTP
     */
    public static String getTokenForHttpCall() {
        AppState appState = AppState.getInstance();
        String token = appState.getAuthToken();
        
        if (token != null) {
            // Usar en headers HTTP
            // Authorization: Bearer <token>
            return "Bearer " + token;
        } else {
            throw new RuntimeException("No hay token disponible");
        }
    }

    /**
     * Ejemplo 3: Acceso desde un Servicio
     */
    public static class FirebaseHttpClientExample {
        
        public void makeAuthenticatedRequest(String endpoint) {
            AppState appState = AppState.getInstance();
            String token = appState.getAuthToken();
            
            if (token == null) {
                throw new RuntimeException("Token no disponible, debe login primero");
            }
            
            // Simulado:
            // HttpClient client = HttpClient.newHttpClient();
            // HttpRequest request = HttpRequest.newBuilder()
            //     .uri(URI.create(endpoint))
            //     .header("Authorization", "Bearer " + token)
            //     .GET()
            //     .build();
            // HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Request a: " + endpoint);
            System.out.println("Con token: " + token.substring(0, 15) + "...");
        }
    }

    /**
     * Ejemplo 4: Logout - Limpiar el estado
     */
    public static void logout() {
        AppState appState = AppState.getInstance();
        appState.clear(); // Limpia token y email
        System.out.println("Sesión cerrada");
        // Navegar de vuelta a login
    }

    /**
     * Ejemplo 5: Cambiar entre pantallas manteniendo token
     */
    public static void switchToMainScreen() {
        AppState appState = AppState.getInstance();
        
        if (!appState.isAuthenticated()) {
            System.out.println("ERROR: Debe autenticarse primero");
            return;
        }
        
        String userEmail = appState.getCurrentUserEmail();
        System.out.println("Bienvenido a la pantalla principal, " + userEmail);
        
        // Cambiar FXML
        // Stage stage = (Stage) loginButton.getScene().getWindow();
        // FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        // Parent root = loader.load();
        // MainController mainController = loader.getController();
        // mainController.setUserEmail(userEmail); // Pasar info del usuario
        // stage.setScene(new Scene(root));
    }

    /**
     * Ejemplo 6: Usar en un Controlador
     */
    public static class MainControllerExample {
        
        public void initialize() {
            AppState appState = AppState.getInstance();
            
            // Verificar que el usuario está autenticado
            if (!appState.isAuthenticated()) {
                System.out.println("ADVERTENCIA: Usuario no autenticado en Main");
                // Redirigir a login
                return;
            }
            
            // Saludar al usuario
            String email = appState.getCurrentUserEmail();
            System.out.println("Hola, " + email);
            
            // Usar token para cargar datos del usuario
            String token = appState.getAuthToken();
            System.out.println("Token para usar en APIs: " + token);
        }
    }

    public static void main(String[] args) {
        // Simulación de flujo de login
        System.out.println("=== Antes de login ===");
        checkAuthentication();
        
        System.out.println("\n=== Simulando login ===");
        AppState appState = AppState.getInstance();
        appState.setAuthToken("mock_token_1234567890");
        appState.setCurrentUserEmail("usuario@prueba.com");
        
        System.out.println("\n=== Después de login ===");
        checkAuthentication();
        
        System.out.println("\n=== Obtener token para HTTP ===");
        System.out.println("Authorization: " + getTokenForHttpCall());
        
        System.out.println("\n=== Hacer request autenticado ===");
        FirebaseHttpClientExample client = new FirebaseHttpClientExample();
        client.makeAuthenticatedRequest("https://api.example.com/games");
        
        System.out.println("\n=== Cambiar a pantalla principal ===");
        switchToMainScreen();
        
        System.out.println("\n=== Logout ===");
        logout();
        
        System.out.println("\n=== Verificar estado después de logout ===");
        checkAuthentication();
    }
}


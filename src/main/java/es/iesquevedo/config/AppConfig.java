package es.iesquevedo.config;


public class AppConfig {
    // Leer de application.properties o hardcodear para desarrollo
    public static final String FIREBASE_ENDPOINT =
            System.getProperty("firebase.endpoint", "https://tu-app.firebaseio.com");

    public static final int FIREBASE_TIMEOUT =
            Integer.parseInt(System.getProperty("firebase.timeout", "10"));
}

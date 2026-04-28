package es.iesquevedo.app;

/**
 * Guarda el estado global de la app en memoria.
 * Singleton simple sin frameworks.
 */
public class AppState {

    private static AppState instance;
    private String idToken;
    private String userEmail;

    private AppState() {}

    public static AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    public void saveToken(String token, String email) {
        this.idToken = token;
        this.userEmail = email;
    }

    public String getIdToken() { return idToken; }
    public String getUserEmail() { return userEmail; }

    public boolean isLoggedIn() { return idToken != null && !idToken.isEmpty(); }

    public void clear() {
        this.idToken = null;
        this.userEmail = null;
    }
}
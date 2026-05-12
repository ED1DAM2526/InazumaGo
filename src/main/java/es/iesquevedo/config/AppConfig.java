package es.iesquevedo.config;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.repository.firebase.GameEventRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.MainService;
import es.iesquevedo.service.GameEventService;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.service.impl.GameEventServiceImpl;
import java.io.IOException;
import java.util.Properties;

/**
 * Clase de configuración ligera del proyecto. Contiene fábricas estáticas para obtener
 * implementaciones de repositorios según la configuración (firebaseUrl).
 * También carga propiedades de application.properties para Firestore y WireMock.
 */
public final class AppConfig {
    private AppConfig() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Crea la implementación del repositorio principal. Si firebaseUrl es null o vacío,
     * se devuelve una implementación en memoria (útil para pruebas locales). Si se
     * proporciona una URL, se devuelve el repositorio orientado a Firebase.
     */
    public static MainRepository createMainRepository(String firebaseUrl) {
        if (firebaseUrl == null || firebaseUrl.trim().isEmpty()) {
            return new InMemoryMainRepository();
        }
        return new FirebaseMainRepository(firebaseUrl);
    }

    /**
     * Atajo para obtener la implementación en memoria (tests).
     */
    public static MainRepository createInMemoryRepository() {
        return new InMemoryMainRepository();
    }

    /**
     * Atajo para obtener la implementación orientada a Firebase (producción).
     */
    public static MainRepository createFirebaseRepository(String firebaseUrl) {
        return new FirebaseMainRepository(firebaseUrl);
    }

    /**
     * Crea el servicio principal a partir del repositorio configurado.
     */
    public static MainService createMainService(String firebaseUrl) {
        return new MainServiceImpl(createMainRepository(firebaseUrl));
    }

    /**
     * Carga las propiedades de configuración desde application.properties
     */
    public static Properties loadProperties() {
        Properties props = new Properties();
        try {
            props.load(AppConfig.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            System.err.println("No se pudo cargar application.properties: " + e.getMessage());
        }
        return props;
    }

    /**
     * Obtiene el endpoint de Firestore desde las propiedades
     */
    public static String getFirestoreEndpoint() {
        Properties props = loadProperties();
        return props.getProperty("firebase.firestore.endpoint",
            "https://firestore.googleapis.com/v1");
    }

    /**
     * Obtiene el timeout de Firestore (en milisegundos)
     */
    public static int getFirestoreTimeout() {
        Properties props = loadProperties();
        return Integer.parseInt(props.getProperty("firebase.firestore.timeout", "30000"));
    }

    /**
     * Obtiene el número de reintentos de Firestore
     */
    public static int getFirestoreRetryAttempts() {
        Properties props = loadProperties();
        return Integer.parseInt(props.getProperty("firebase.retry-attempts", "3"));
    }

    /**
     * Obtiene la URL base de WireMock para testing
     */
    public static String getWireMockBaseUrl() {
        Properties props = loadProperties();
        return props.getProperty("wiremock.server.baseurl", "http://localhost:8080");
    }

    /**
     * Obtiene el puerto de WireMock
     */
    public static int getWireMockPort() {
        Properties props = loadProperties();
        return Integer.parseInt(props.getProperty("wiremock.server.port", "8080"));
    }

    /**
     * Crea el repositorio de eventos de juego con una URL de Firebase
     */
    public static GameEventRepository createGameEventRepository(String firebaseUrl) {
        return new GameEventRepository(firebaseUrl);
    }

    /**
     * Crea el repositorio de eventos de juego desde un Firebase Database mockeado (para tests)
     */
    public static GameEventRepository createGameEventRepositoryFromDatabase(com.google.firebase.database.FirebaseDatabase database) {
        return new GameEventRepository(database);
    }

    /**
     * Crea el servicio de eventos de juego
     */
    public static GameEventService createGameEventService(String firebaseUrl) {
        GameEventRepository repository = createGameEventRepository(firebaseUrl);
        return new GameEventServiceImpl(repository);
    }

    /**
     * Crea el servicio de eventos de juego desde un repositorio mockeado (para tests)
     */
    public static GameEventService createGameEventService(GameEventRepository repository) {
        return new GameEventServiceImpl(repository);
    }
}

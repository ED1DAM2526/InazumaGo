package es.iesquevedo.config;

/**
 * Clase de configuración ligera del proyecto. Contiene fábricas estáticas para obtener
 * implementaciones de repositorios según la configuración (firebaseUrl).
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
    public static es.iesquevedo.repository.MainRepository createMainRepository(String firebaseUrl) {
        if (firebaseUrl == null || firebaseUrl.isBlank()) {
            return new es.iesquevedo.repository.inmemory.InMemoryMainRepository();
        }
        return new es.iesquevedo.repository.firebase.FirebaseMainRepository(firebaseUrl);
    }

    /**
     * Atajo para obtener la implementación en memoria (tests).
     */
    public static es.iesquevedo.repository.MainRepository createInMemoryRepository() {
        return new es.iesquevedo.repository.inmemory.InMemoryMainRepository();
    }

    /**
     * Atajo para obtener la implementación orientada a Firebase (producción).
     */
    public static es.iesquevedo.repository.MainRepository createFirebaseRepository(String firebaseUrl) {
        return new es.iesquevedo.repository.firebase.FirebaseMainRepository(firebaseUrl);
    }
}

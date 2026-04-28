package es.iesquevedo.config;

import es.iesquevedo.repository.MainRepository;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import es.iesquevedo.service.MainService;
import es.iesquevedo.service.impl.MainServiceImpl;

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
    public static MainRepository createMainRepository(String firebaseUrl) {
        if (firebaseUrl == null || firebaseUrl.isBlank()) {
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
}

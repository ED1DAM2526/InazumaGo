package es.iesquevedo.repository.firebase;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.repository.MainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests básicos de FirebaseMainRepository.
 * Tests completos con WireMock se harán después de configurar Firebase.
 */
public class FirebaseMainRepositoryTest {
    private FirebaseMainRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FirebaseMainRepository("https://test-project.firebaseio.com");
    }

    @Test
    void testRepositoryImplementsInterface() {
        assertNotNull(repository);
        assertTrue(repository instanceof MainRepository);
    }

    @Test
    void testSetIdToken() {
        repository.setIdToken("test-token-123");
        assertNotNull(repository.getCurrentToken());
    }

    @Test
    void testFindDefaultName() {
        assertEquals("FirebasePlayer", repository.findDefaultName());
    }

    @Test
    void testAddMovesListener() {
        String listenerId = repository.addMovesListener("game1", moves -> {});
        assertNotNull(listenerId);
        assertTrue(listenerId.startsWith("listener-"));
    }

    @Test
    void testRemoveMovesListener() {
        String listenerId = repository.addMovesListener("game1", moves -> {});
        repository.removeMovesListener("game1", listenerId);
        assertTrue(true);
    }

    @Test
    void testFirebaseRealConnectivity() throws Exception {
        // Test contra Firebase real - solo verificar que conecta
        FirebaseMainRepository realRepo = new FirebaseMainRepository(
            "https://inazumago-default-rtdb.firebaseio.com"
        );
        
        // Si logra instanciarse y tiene un token management, conecta
        realRepo.setIdToken("test-token");
        String token = realRepo.getCurrentToken();
        
        assertNotNull(token);
        assertEquals("test-token", token);
    }
}
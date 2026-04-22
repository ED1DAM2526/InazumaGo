package es.iesquevedo.repository.firebase;

import com.google.firebase.database.FirebaseDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FirebaseMainRepositoryTest {

    private FirebaseMainRepository repository;

    @BeforeEach
    void setUp() {
        // Se inyecta un mock de FirebaseDatabase para evitar depender de una app Firebase real
        FirebaseDatabase mockDatabase = mock(FirebaseDatabase.class);
        repository = new FirebaseMainRepository(mockDatabase);
    }

    @Test
    void testPatchMultiPathSuccess() throws Exception {
        Map<String, Object> updates = Map.of("meta/turn", 2);
        boolean result = repository.patchMultiPath("games/test", updates);
        assertTrue(result);
    }
}
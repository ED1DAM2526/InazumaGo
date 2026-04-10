package es.iesquevedo.repository.firebase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class FirebaseMainRepositoryTest {

    private FirebaseMainRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FirebaseMainRepository("http://localhost:8080");
    }

    @Test
    void testPatchMultiPathSuccess() throws Exception {
        Map<String, Object> updates = Map.of("meta/turn", 2);
        boolean result = repository.patchMultiPath("games/test", updates);
        assertTrue(result);
    }
}
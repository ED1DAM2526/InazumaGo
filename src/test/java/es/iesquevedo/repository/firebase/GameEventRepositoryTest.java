package es.iesquevedo.repository.firebase;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el repositorio de eventos de juego.
 * Verifica que los eventos se registren correctamente con Firebase.
 */
@ExtendWith(MockitoExtension.class)
class GameEventRepositoryTest {

    @Mock
    private FirebaseDatabase firebaseDatabase;

    @Mock
    private DatabaseReference databaseReference;

    @Mock
    private DatabaseReference eventsReference;

    private GameEventRepository gameEventRepository;

    @BeforeEach
    void setUp() {
        gameEventRepository = new GameEventRepository(firebaseDatabase);
        when(firebaseDatabase.getReference("game_events")).thenReturn(eventsReference);
    }

    /**
     * Test: Registrar evento de inicio de partida
     */
    @Test
    void testRecordGameStart_shouldReturnCompletableFuture() {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );

        DatabaseReference pushRef = mock(DatabaseReference.class);
        when(eventsReference.push()).thenReturn(pushRef);

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameStart(gameId, gameDto);

        // Assert
        assertNotNull(result);
        verify(eventsReference).push();
    }

    /**
     * Test: Registrar evento de movimiento
     */
    @Test
    void testRecordGameMove_shouldReturnCompletableFuture() {
        // Arrange
        String gameId = "game123";
        Position position = new Position(5, 8);
        MoveData moveData = new MoveData(
            "player1",
            "KICK",
            position
        );

        DatabaseReference pushRef = mock(DatabaseReference.class);
        when(eventsReference.push()).thenReturn(pushRef);

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameMove(gameId, moveData);

        // Assert
        assertNotNull(result);
        verify(eventsReference).push();
    }

    /**
     * Test: Registrar evento de fin de partida
     */
    @Test
    void testRecordGameEnd_shouldReturnCompletableFuture() {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "FINISHED",
            System.currentTimeMillis()
        );

        DatabaseReference pushRef = mock(DatabaseReference.class);
        when(eventsReference.push()).thenReturn(pushRef);

        // Act
        CompletableFuture<Void> result = gameEventRepository.recordGameEnd(gameId, gameDto);

        // Assert
        assertNotNull(result);
        verify(eventsReference).push();
    }

    /**
     * Test: Verificar que el evento incluye el tipo correcto
     */
    @Test
    void testEventTypeIsCorrect() {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(gameId, "Test Game", Arrays.asList("P1"), "IN_PROGRESS", System.currentTimeMillis());

        DatabaseReference pushRef = mock(DatabaseReference.class);
        when(eventsReference.push()).thenReturn(pushRef);

        // Capturar el valor que se pasa a setValue
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // Act
        gameEventRepository.recordGameStart(gameId, gameDto);

        // Assert
        verify(pushRef).setValue(valueCaptor.capture(), any());
    }

    /**
     * Test: Verificar que los eventos se registran con timestamp
     */
    @Test
    void testEventIncludesTimestamp() {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(gameId, "Test Game", Arrays.asList("P1"), "IN_PROGRESS", System.currentTimeMillis());

        long beforeRecording = System.currentTimeMillis();
        DatabaseReference pushRef = mock(DatabaseReference.class);
        when(eventsReference.push()).thenReturn(pushRef);

        // Act
        gameEventRepository.recordGameStart(gameId, gameDto);

        long afterRecording = System.currentTimeMillis();

        // Assert
        assertTrue(afterRecording >= beforeRecording);
        verify(pushRef).setValue(any(), any());
    }

    /**
     * Test: Verificar que el repositorio usa el tipo correcto de evento
     */
    @Test
    void testEventTypeEnumValues() {
        // Assert
        assertEquals("game.start", GameEventRepository.EventType.GAME_START.getValue());
        assertEquals("game.move", GameEventRepository.EventType.GAME_MOVE.getValue());
        assertEquals("game.end", GameEventRepository.EventType.GAME_END.getValue());
    }

}


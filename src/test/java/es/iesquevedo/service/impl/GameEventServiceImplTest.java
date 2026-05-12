package es.iesquevedo.service.impl;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.dto.Position;
import es.iesquevedo.repository.firebase.GameEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el servicio de eventos de juego.
 * Verifica que los eventos se sincronicen correctamente de forma asíncrona.
 */
@ExtendWith(MockitoExtension.class)
class GameEventServiceImplTest {

    @Mock
    private GameEventRepository eventRepository;

    private GameEventServiceImpl gameEventService;

    @BeforeEach
    void setUp() {
        gameEventService = new GameEventServiceImpl(eventRepository);
    }

    @AfterEach
    void tearDown() {
        gameEventService.shutdown();
    }

    /**
     * Test: Notificar inicio de partida
     */
    @Test
    void testNotifyGameStart_shouldCallRepository() throws Exception {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "IN_PROGRESS",
            System.currentTimeMillis()
        );

        when(eventRepository.recordGameStart(gameId, gameDto))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<Void> result = gameEventService.notifyGameStart(gameId, gameDto);

        // Assert
        assertNotNull(result);
        result.get(1, TimeUnit.SECONDS);
        verify(eventRepository).recordGameStart(gameId, gameDto);
    }

    /**
     * Test: Notificar movimiento
     */
    @Test
    void testNotifyGameMove_shouldCallRepository() throws Exception {
        // Arrange
        String gameId = "game123";
        Position position = new Position(5, 8);
        MoveData moveData = new MoveData(
            "player1",
            "KICK",
            position
        );

        when(eventRepository.recordGameMove(gameId, moveData))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<Void> result = gameEventService.notifyGameMove(gameId, moveData);

        // Assert
        assertNotNull(result);
        result.get(1, TimeUnit.SECONDS);
        verify(eventRepository).recordGameMove(gameId, moveData);
    }

    /**
     * Test: Notificar fin de partida
     */
    @Test
    void testNotifyGameEnd_shouldCallRepository() throws Exception {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(
            gameId,
            "Final Cup",
            Arrays.asList("Player1", "Player2"),
            "FINISHED",
            System.currentTimeMillis()
        );

        when(eventRepository.recordGameEnd(gameId, gameDto))
            .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        CompletableFuture<Void> result = gameEventService.notifyGameEnd(gameId, gameDto);

        // Assert
        assertNotNull(result);
        result.get(1, TimeUnit.SECONDS);
        verify(eventRepository).recordGameEnd(gameId, gameDto);
    }

    /**
     * Test: Las notificaciones son asíncronas
     */
    @Test
    void testNotificationsAreAsynchronous() throws InterruptedException {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(gameId, "Test", Arrays.asList("P1"), "IN_PROGRESS", System.currentTimeMillis());

        CompletableFuture<Void> slowFuture = new CompletableFuture<>();
        when(eventRepository.recordGameStart(gameId, gameDto))
            .thenReturn(slowFuture);

        // Act
        CompletableFuture<Void> result = gameEventService.notifyGameStart(gameId, gameDto);

        // Assert - La operación no debería estar bloqueada
        Thread.sleep(100); // Pequeña espera
        assertNotNull(result);
    }

    /**
     * Test: Manejo de errores en la notificación
     */
    @Test
    void testNotifyGameStart_handlesException() {
        // Arrange
        String gameId = "game123";
        GameDto gameDto = new GameDto(gameId, "Test", Arrays.asList("P1"), "IN_PROGRESS", System.currentTimeMillis());

        when(eventRepository.recordGameStart(gameId, gameDto))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Test error")));

        // Act
        CompletableFuture<Void> result = gameEventService.notifyGameStart(gameId, gameDto);

        // Assert - El future contiene el error
        assertThrows(RuntimeException.class, result::join);
    }

}


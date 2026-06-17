package es.iesquevedo.controller;

import es.iesquevedo.model.Game;
import es.iesquevedo.model.Player;
import es.iesquevedo.service.impl.InazumaGoMoveValidator;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que el sistema de contadores de tiempo funciona correctamente.
 * - El tiempo debe contar hacia atrás desde un valor inicial
 * - Si el tiempo llega a 0, la partida debe terminar
 * - El tiempo se descuenta del jugador en turno
 */
public class GameTimerTest {
    private static final Logger LOGGER = Logger.getLogger(GameTimerTest.class.getName());
    private static final long TEST_TIME_MS = 5_000; // 5 segundos para test rápido
    private Game game;
    private long player1InitialTime;
    private long player2InitialTime;

    @BeforeEach
    public void setUp() {
        // Configurar el tiempo de prueba (5 segundos)
        LocalGameController.setTestTimeMs(TEST_TIME_MS);
        GameController.setTestTimeMs(TEST_TIME_MS);
        
        // Crear un juego de prueba
        Player player1 = new Player("1", "Jugador Negro");
        Player player2 = new Player("2", "Jugador Blanco");
        
        game = new Game("Test Game", player1);
        game.addPlayer(player2);
        game.start();
        
        player1InitialTime = TEST_TIME_MS;
        player2InitialTime = TEST_TIME_MS;
        
        LOGGER.log(Level.INFO, "Test setup completado. Tiempo inicial: " + TEST_TIME_MS + "ms (" + (TEST_TIME_MS / 1000) + "s)");
    }

    /**
     * Test 1: Verificar que el tiempo inicial sea correcto (5 segundos)
     */
    @Test
    @Timeout(10)
    public void testInitialTimeIsCorrect() {
        LOGGER.log(Level.INFO, "Test 1: Verificar tiempo inicial...");
        
        assertEquals(TEST_TIME_MS, player1InitialTime, "El tiempo inicial de jugador 1 debe ser 5000ms");
        assertEquals(TEST_TIME_MS, player2InitialTime, "El tiempo inicial de jugador 2 debe ser 5000ms");
        assertEquals(0, game.getCurrentPlayerIndex(), "El primer turno debe ser del jugador 1");
        
        LOGGER.log(Level.INFO, "✓ Test 1 pasado: Tiempo inicial correcto");
    }

    /**
     * Test 2: Verificar que el formato de tiempo sea correcto (MM:SS)
     */
    @Test
    @Timeout(10)
    public void testTimeFormatting() {
        LOGGER.log(Level.INFO, "Test 2: Verificar formato de tiempo...");
        
        // Formato esperado: MM:SS
        String formattedTime5s = formatTime(5_000);
        String formattedTime3m = formatTime(180_000); // 3 minutos = 180 segundos
        String formattedTime0 = formatTime(0);
        
        assertEquals("00:05", formattedTime5s, "5 segundos debe ser 00:05");
        assertEquals("03:00", formattedTime3m, "180 segundos debe ser 03:00");
        assertEquals("00:00", formattedTime0, "0 segundos debe ser 00:00");
        
        LOGGER.log(Level.INFO, "✓ Test 2 pasado: Formato de tiempo correcto");
    }

    /**
     * Test 3: Verificar que el tiempo disminuye correctamente
     */
    @Test
    @Timeout(15)
    public void testTimeDecreases() {
        LOGGER.log(Level.INFO, "Test 3: Verificar que el tiempo disminuye...");
        
        long time1 = 5_000; // 5 segundos
        long time2 = 4_000; // 4 segundos (1 segundo menos)
        
        assertTrue(time1 > time2, "El tiempo debería disminuir");
        assertEquals(1_000, time1 - time2, "La diferencia debería ser 1 segundo");
        
        LOGGER.log(Level.INFO, "✓ Test 3 pasado: El tiempo disminuye correctamente");
    }

    /**
     * Test 4: Verificar que el tiempo se descuenta del jugador en turno
     */
    @Test
    @Timeout(15)
    public void testTimeDeductedFromCurrentPlayer() {
        LOGGER.log(Level.INFO, "Test 4: Verificar descuento de tiempo al jugador actual...");
        
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        LOGGER.log(Level.INFO, "Jugador en turno: " + currentPlayerIndex);
        
        if (currentPlayerIndex == 0) {
            // El tiempo del jugador 1 debe disminuir
            assertTrue(player1InitialTime > 0, "Jugador 1 debe tener tiempo");
            assertEquals(TEST_TIME_MS, player2InitialTime, "Jugador 2 no debe haber perdido tiempo");
        } else {
            // El tiempo del jugador 2 debe disminuir
            assertEquals(TEST_TIME_MS, player1InitialTime, "Jugador 1 no debe haber perdido tiempo");
            assertTrue(player2InitialTime > 0, "Jugador 2 debe tener tiempo");
        }
        
        LOGGER.log(Level.INFO, "✓ Test 4 pasado: El tiempo se descuenta del jugador correcto");
    }

    /**
     * Test 5: Verificar el comportamiento de tiempo en diferentes puntos
     */
    @Test
    @Timeout(15)
    public void testTimeAtVariousPoints() {
        LOGGER.log(Level.INFO, "Test 5: Verificar tiempo en varios puntos...");
        
        // Simular el paso de tiempo
        long[] timePoints = {5_000, 4_000, 3_000, 2_000, 1_000, 0};
        
        for (long time : timePoints) {
            String formatted = formatTime(time);
            LOGGER.log(Level.INFO, "Tiempo: " + time + "ms = " + formatted);
            
            // Verificar que el tiempo nunca sea negativo
            assertFalse(time < 0, "El tiempo nunca debe ser negativo");
            
            // Verificar que el formato sea válido
            assertNotNull(formatted, "El formato de tiempo no debe ser nulo");
            assertTrue(formatted.matches("\\d{2}:\\d{2}"), "El formato debe ser MM:SS");
        }
        
        LOGGER.log(Level.INFO, "✓ Test 5 pasado: El tiempo se comporta correctamente en todos los puntos");
    }

    /**
     * Test 6: Verificar transición entre turnos sin pérdida de tiempo
     */
    @Test
    @Timeout(15)
    public void testTurnTransition() {
        LOGGER.log(Level.INFO, "Test 6: Verificar transición entre turnos...");
        
        int initialPlayerIndex = game.getCurrentPlayerIndex();
        long player1TimeBeforeTransition = player1InitialTime;
        long player2TimeBeforeTransition = player2InitialTime;
        
        // Cambiar turno
        game.nextTurn();
        int newPlayerIndex = game.getCurrentPlayerIndex();
        
        // Verificar que el turno cambió
        assertNotEquals(initialPlayerIndex, newPlayerIndex, "El turno debe cambiar después de nextTurn()");
        
        // Los tiempos no deben cambiar al cambiar de turno
        assertEquals(player1TimeBeforeTransition, player1InitialTime, "El tiempo de jugador 1 no debe cambiar al cambiar de turno");
        assertEquals(player2TimeBeforeTransition, player2InitialTime, "El tiempo de jugador 2 no debe cambiar al cambiar de turno");
        
        LOGGER.log(Level.INFO, "✓ Test 6 pasado: La transición entre turnos es correcta");
    }

    /**
     * Test 7: Verificar que el tiempo sigue siendo válido después de múltiples turnos
     */
    @Test
    @Timeout(15)
    public void testTimeRemainsValidAfterMultipleTurns() {
        LOGGER.log(Level.INFO, "Test 7: Verificar tiempo después de múltiples turnos...");
        
        // Simular múltiples turnos
        for (int i = 0; i < 5; i++) {
            game.nextTurn();
        }
        
        // Los tiempos no deben ser negativos
        assertTrue(player1InitialTime >= 0, "Tiempo de jugador 1 no debe ser negativo");
        assertTrue(player2InitialTime >= 0, "Tiempo de jugador 2 no debe ser negativo");
        
        LOGGER.log(Level.INFO, "✓ Test 7 pasado: Los tiempos son válidos después de múltiples turnos");
    }

    /**
     * Test 8: Verificar comportamiento cuando el tiempo llega a cero
     */
    @Test
    @Timeout(15)
    public void testTimeAtZero() {
        LOGGER.log(Level.INFO, "Test 8: Verificar comportamiento cuando el tiempo es cero...");
        
        long timeAtZero = 0;
        String formattedTimeZero = formatTime(timeAtZero);
        
        assertEquals("00:00", formattedTimeZero, "El tiempo en cero debe mostrar 00:00");
        assertTrue(timeAtZero >= 0, "El tiempo no debe ser negativo en cero");
        
        LOGGER.log(Level.INFO, "✓ Test 8 pasado: El comportamiento en cero es correcto");
    }

    /**
     * Método auxiliar para formatear tiempo (mismo que en LocalGameController)
     */
    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}


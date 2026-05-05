package es.iesquevedo.service.impl;

import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.MoveData;
import es.iesquevedo.repository.firebase.GameEventRepository;
import es.iesquevedo.service.GameEventService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementación del servicio de eventos de juego.
 * Maneja la sincronización asíncrona de eventos de partida con Firebase.
 */
public class GameEventServiceImpl implements GameEventService {

    private final GameEventRepository eventRepository;
    private final ExecutorService executorService;

    /**
     * Constructor que inyecta el repositorio de eventos
     */
    public GameEventServiceImpl(GameEventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public CompletableFuture<Void> notifyGameStart(String gameId, GameDto gameDto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return eventRepository.recordGameStart(gameId, gameDto).join();
            } catch (Exception e) {
                throw new RuntimeException("Error al notificar inicio de partida: " + e.getMessage(), e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> notifyGameMove(String gameId, MoveData moveData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return eventRepository.recordGameMove(gameId, moveData).join();
            } catch (Exception e) {
                throw new RuntimeException("Error al notificar movimiento: " + e.getMessage(), e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Void> notifyGameEnd(String gameId, GameDto gameDto) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return eventRepository.recordGameEnd(gameId, gameDto).join();
            } catch (Exception e) {
                throw new RuntimeException("Error al notificar fin de partida: " + e.getMessage(), e);
            }
        }, executorService);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}


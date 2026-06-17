package es.iesquevedo.service.impl;

import es.iesquevedo.config.AppState;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.dto.RemoteMoveDto;
import es.iesquevedo.model.Board;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Player;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.service.MultiplayerGameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación de MultiplayerGameService usando Firebase Realtime Database.
 * Sincroniza partidas entre múltiples dispositivos en tiempo real.
 */
public class MultiplayerGameServiceImpl implements MultiplayerGameService {
    private static final Logger LOGGER = Logger.getLogger(MultiplayerGameServiceImpl.class.getName());

    private final FirebaseMainRepository repository;
    private final Map<String, Game> gameCache = new ConcurrentHashMap<>();
    private final Map<String, String> movesListenerIds = new ConcurrentHashMap<>();
    private final Map<String, String> gameStateListenerIds = new ConcurrentHashMap<>();
    private final Map<String, List<RemoteMoveDto>> remoteMoveCache = new ConcurrentHashMap<>();

    public MultiplayerGameServiceImpl(FirebaseMainRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompletableFuture<String> createMultiplayerGame(String gameName, Player player1) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String gameId = UUID.randomUUID().toString();
                GameDto gameDto = new GameDto();
                gameDto.setId(gameId);
                gameDto.setName(gameName);
                gameDto.setStatus("WAITING");
                gameDto.setCreatedAt(System.currentTimeMillis());
                gameDto.setPlayers(List.of(player1.getId()));

                repository.createGame(gameDto).get();
                LOGGER.log(Level.INFO, "Partida multijugador creada: " + gameId);
                return gameId;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al crear partida multijugador", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Game> joinMultiplayerGame(String gameId, Player player2) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                GameDto gameDto = repository.getGame(gameId).get();
                if (gameDto == null) {
                    throw new IllegalStateException("Partida no encontrada: " + gameId);
                }

                // Actualizar lista de jugadores
                List<String> players = new ArrayList<>(gameDto.getPlayers());
                if (players.size() >= 2) {
                    throw new IllegalStateException("La partida ya está llena");
                }
                players.add(player2.getId());
                gameDto.setPlayers(players);
                gameDto.setStatus("READY");

                // Actualizar en Firebase
                repository.updateGame(gameId, gameDto).get();

                // Convertir a modelo local
                Game game = convertDtoToGame(gameDto);
                gameCache.put(gameId, game);

                LOGGER.log(Level.INFO, "Jugador " + player2.getName() + " se unió a partida: " + gameId);
                return game;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al unirse a partida multijugador", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> startMultiplayerGame(String gameId) {
        return CompletableFuture.runAsync(() -> {
            try {
                GameDto gameDto = repository.getGame(gameId).get();
                if (gameDto == null) {
                    throw new IllegalStateException("Partida no encontrada");
                }
                if (gameDto.getPlayers().size() < 2) {
                    throw new IllegalStateException("Se necesitan 2 jugadores para iniciar");
                }

                gameDto.setStatus("IN_PROGRESS");
                repository.updateGame(gameId, gameDto).get();

                Game game = convertDtoToGame(gameDto);
                gameCache.put(gameId, game);

                LOGGER.log(Level.INFO, "Partida multijugador iniciada: " + gameId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al iniciar partida multijugador", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> sendRemoteMove(String gameId, RemoteMoveDto move) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Generar ID único para el movimiento
                move.setMoveId(UUID.randomUUID().toString());
                move.setGameId(gameId);
                move.setTimestamp(System.currentTimeMillis());

                // Guardar movimiento en Firebase bajo /games/{gameId}/remoteMoves/{moveId}
                // Esto se hará a través del repositorio en una actualización futura
                // Por ahora, registrar en caché local
                remoteMoveCache.computeIfAbsent(gameId, k -> new ArrayList<>()).add(move);

                LOGGER.log(Level.INFO, "Movimiento remoto enviado - Partida: " + gameId + 
                    ", Jugador: " + move.getPlayerId() + ", Posición: [" + move.getRow() + "," + move.getCol() + "]");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al enviar movimiento remoto", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public String subscribeToRemoteMoves(String gameId, Consumer<List<RemoteMoveDto>> listener) {
        String listenerId = "remote_moves_" + UUID.randomUUID();
        
        // Simular listener con polling (en producción usar Firebase listeners reales)
        Thread listeningThread = new Thread(() -> {
            while (movesListenerIds.containsKey(gameId)) {
                try {
                    List<RemoteMoveDto> moves = remoteMoveCache.getOrDefault(gameId, new ArrayList<>());
                    listener.accept(moves);
                    Thread.sleep(500); // Poll cada 500ms
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();

        movesListenerIds.put(gameId, listenerId);
        LOGGER.log(Level.INFO, "Listener de movimientos remotos suscrito: " + listenerId);
        return listenerId;
    }

    @Override
    public String subscribeToGameState(String gameId, Consumer<Game> listener) {
        String listenerId = "game_state_" + UUID.randomUUID();

        Thread listeningThread = new Thread(() -> {
            while (gameStateListenerIds.containsKey(gameId)) {
                try {
                    Game game = gameCache.get(gameId);
                    if (game != null) {
                        listener.accept(game);
                    }
                    Thread.sleep(1000); // Poll cada segundo
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        listeningThread.setDaemon(true);
        listeningThread.start();

        gameStateListenerIds.put(gameId, listenerId);
        LOGGER.log(Level.INFO, "Listener de estado de partida suscrito: " + listenerId);
        return listenerId;
    }

    @Override
    public CompletableFuture<Game> getGameState(String gameId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Game cachedGame = gameCache.get(gameId);
                if (cachedGame != null) {
                    return cachedGame;
                }

                GameDto gameDto = repository.getGame(gameId).get();
                if (gameDto == null) {
                    return null;
                }

                Game game = convertDtoToGame(gameDto);
                gameCache.put(gameId, game);
                return game;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al obtener estado de partida", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void unsubscribeFromRemoteMoves(String gameId, String listenerId) {
        movesListenerIds.remove(gameId);
        LOGGER.log(Level.INFO, "Desuscrito de movimientos remotos: " + listenerId);
    }

    @Override
    public void unsubscribeFromGameState(String gameId, String listenerId) {
        gameStateListenerIds.remove(gameId);
        LOGGER.log(Level.INFO, "Desuscrito de estado de partida: " + listenerId);
    }

    @Override
    public CompletableFuture<Void> finishMultiplayerGame(String gameId, String winnerId) {
        return CompletableFuture.runAsync(() -> {
            try {
                GameDto gameDto = repository.getGame(gameId).get();
                if (gameDto != null) {
                    gameDto.setStatus("FINISHED");
                    repository.updateGame(gameId, gameDto).get();

                    Game game = convertDtoToGame(gameDto);
                    gameCache.put(gameId, game);

                    LOGGER.log(Level.INFO, "Partida multijugador finalizada: " + gameId + ", Ganador: " + winnerId);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al finalizar partida multijugador", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> abandonMultiplayerGame(String gameId) {
        return CompletableFuture.runAsync(() -> {
            try {
                GameDto gameDto = repository.getGame(gameId).get();
                if (gameDto != null) {
                    gameDto.setStatus("ABANDONED");
                    repository.updateGame(gameId, gameDto).get();

                    Game game = convertDtoToGame(gameDto);
                    gameCache.put(gameId, game);

                    LOGGER.log(Level.INFO, "Partida multijugador abandonada: " + gameId);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al abandonar partida multijugador", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<String>> getAvailableGames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<GameDto> games = repository.listGames().get();
                List<String> availableGameIds = new ArrayList<>();

                if (games != null) {
                    for (GameDto game : games) {
                        if ("WAITING".equals(game.getStatus()) && game.getPlayers().size() < 2) {
                            availableGameIds.add(game.getId());
                        }
                    }
                }

                LOGGER.log(Level.INFO, "Partidas disponibles encontradas: " + availableGameIds.size());
                return availableGameIds;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al obtener partidas disponibles", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Convierte un GameDto a modelo Game local.
     */
    private Game convertDtoToGame(GameDto dto) {
        if (dto == null || dto.getPlayers() == null || dto.getPlayers().isEmpty()) {
            return null;
        }

        Player player1 = new Player(dto.getPlayers().get(0), "Jugador 1");
        Game game = new Game(dto.getName(), player1);
        game.setId(dto.getId());

        if (dto.getPlayers().size() > 1) {
            Player player2 = new Player(dto.getPlayers().get(1), "Jugador 2");
            game.addPlayer(player2);
        }

        if ("IN_PROGRESS".equals(dto.getStatus())) {
            game.setState(GameState.IN_PROGRESS);
        } else if ("FINISHED".equals(dto.getStatus())) {
            game.setState(GameState.FINISHED);
        } else if ("ABANDONED".equals(dto.getStatus())) {
            game.setState(GameState.ABANDONED);
        }

        return game;
    }
}


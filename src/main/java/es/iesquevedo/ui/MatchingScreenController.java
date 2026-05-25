package es.iesquevedo.ui;

import es.iesquevedo.config.AppState;
import es.iesquevedo.controller.GameController;
import es.iesquevedo.dto.GameDto;
import es.iesquevedo.model.Game;
import es.iesquevedo.model.GameState;
import es.iesquevedo.model.Player;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.service.GameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MatchingScreenController {

    private static final Logger LOGGER = Logger.getLogger(MatchingScreenController.class.getName());
    private static final String FIREBASE_URL = "https://inazumago-default-rtdb.firebaseio.com";

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button searchButton;

    private GameService gameService = new GameServiceImpl();
    private FirebaseMainRepository firebaseRepository;
    private Player currentPlayer;
    private Timer timer;
    private int elapsedSeconds = 0;
    private CompletableFuture<?> currentSearch;
    private volatile boolean searching = true;
    private String createdGameId;  // Juego creado por este jugador

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void startMatching(Player player) {
        this.currentPlayer = player;
        if (this.gameService == null) {
            this.gameService = new GameServiceImpl();
        }
        
        // Inicializar Firebase
        this.firebaseRepository = new FirebaseMainRepository(FIREBASE_URL);
        String token = AppState.getInstance().getAuthToken();
        
        LOGGER.log(java.util.logging.Level.INFO, "Token en AppState: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));
        
        if (token != null) {
            firebaseRepository.setIdToken(token);
            LOGGER.log(java.util.logging.Level.INFO, "Token configurado en Firebase Repository");
        } else {
            LOGGER.log(java.util.logging.Level.WARNING, "⚠️ Token es NULL en AppState");
        }
        
        updateReadyState();
    }

    @FXML
    private void onSearchClicked() {
        if (currentPlayer == null) {
            statusLabel.setText("No hay jugador autenticado");
            return;
        }
        if (searching) {
            return;
        }
        searching = true;
        elapsedSeconds = 0;
        startTimer();
        startSearch();
    }

    private void startTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    elapsedSeconds++;
                    timeLabel.setText("Tiempo de espera: " + elapsedSeconds + "s");
                });
            }
        }, 1000, 1000);
    }

    private void startSearch() {
        statusLabel.setText("Buscando partida en Firebase...");
        if (searchButton != null) {
            searchButton.setDisable(true);
        }
        if (cancelButton != null) {
            cancelButton.setText("Cancelar");
        }

        // Buscar juegos disponibles (WAITING = sin oponente)
        currentSearch = firebaseRepository.listGames()
            .thenAccept(gameDtos -> {
                if (!searching) return;
                
                // Filtrar juegos WAITING
                GameDto waitingGame = gameDtos.stream()
                    .filter(g -> "WAITING".equals(g.getStatus()))
                    .findFirst()
                    .orElse(null);
                
                if (waitingGame != null) {
                    // ¡Encontramos un juego esperando! Unirnos
                    Platform.runLater(() -> joinExistingGame(waitingGame.getId()));
                } else {
                    // No hay juegos disponibles, crear uno nuevo
                    Platform.runLater(this::createNewGame);
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error buscando partidas: " + ex.getMessage());
                    LOGGER.log(Level.WARNING, "Error en búsqueda: " + ex.getMessage());
                });
                return null;
            });
    }

    private void createNewGame() {
        statusLabel.setText("Creando partida nueva. Esperando oponente...");
        startTimer();
        
        // Crear juego en Firebase
        String gameId = "game_" + UUID.randomUUID().toString();
        createdGameId = gameId;
        
        GameDto newGame = new GameDto();
        newGame.setId(gameId);
        newGame.setBlackPlayer(currentPlayer.getId());
        newGame.setWhitePlayer(null);  // Sin oponente aún
        newGame.setStatus("WAITING");
        newGame.setCurrentTurn("BLACK");
        newGame.setMoves(new java.util.ArrayList<>());
        newGame.setBoard(new int[9][9]);  // Board vacío
        newGame.setCreatedAt(System.currentTimeMillis());  // Timestamp actual
        
        LOGGER.log(java.util.logging.Level.INFO, "Enviando GameDto a Firebase: " + com.google.gson.Gson.class.getProtectionDomain() + "; idToken presente: " + (firebaseRepository != null));
        
        firebaseRepository.createGame(newGame)
            .thenAccept(created -> {
                LOGGER.log(Level.INFO, "Juego creado en Firebase: " + gameId);
                
                // Esperar a que otro jugador se una (polling)
                pollForOpponent(gameId);
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error creando partida: " + ex.getMessage());
                    searching = false;
                    if (searchButton != null) searchButton.setDisable(false);
                });
                return null;
            });
    }

    private void pollForOpponent(String gameId) {
        // Polling cada 2 segundos para ver si alguien se unió
        Timer pollTimer = new Timer(true);
        pollTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!searching || createdGameId == null) {
                    pollTimer.cancel();
                    return;
                }
                
                firebaseRepository.getGame(gameId)
                    .thenAccept(game -> {
                        if (game != null && game.getWhitePlayer() != null) {
                            // ¡Se unió un oponente!
                            Platform.runLater(() -> {
                                stopTimer();
                                pollTimer.cancel();
                                navigateToGame(gameId);
                            });
                        }
                    })
                    .exceptionally(ex -> {
                        LOGGER.log(Level.WARNING, "Error en polling: " + ex.getMessage());
                        return null;
                    });
            }
        }, 0, 2000);
    }

    private void joinExistingGame(String gameId) {
        statusLabel.setText("¡Partida encontrada! Uniéndote...");
        stopTimer();
        
        // Obtener el juego actual
        firebaseRepository.getGame(gameId)
            .thenAccept(game -> {
                if (game != null && game.getWhitePlayer() == null) {
                    // Actualizar como jugador blanco
                    game.setWhitePlayer(currentPlayer.getId());
                    game.setStatus("IN_PROGRESS");
                    
                    firebaseRepository.updateGame(gameId, game)
                        .thenAccept(updated -> {
                            Platform.runLater(() -> {
                                navigateToGame(gameId);
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                statusLabel.setText("Error al unirse: " + ex.getMessage());
                                searching = false;
                                if (searchButton != null) searchButton.setDisable(false);
                            });
                            return null;
                        });
                } else {
                    Platform.runLater(() -> {
                        statusLabel.setText("La partida ya tiene oponente. Buscando otra...");
                        startSearch();
                    });
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error obteniendo juego: " + ex.getMessage());
                });
                return null;
            });
    }

    private void navigateToGame(String gameId) {
        try {
            searching = false;
            if (currentSearch != null) {
                currentSearch.cancel(true);
            }
            createdGameId = null;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Parent root = loader.load();

            GameController gameController = loader.getController();
            String playerEmail = currentPlayer != null ? currentPlayer.getId() : "Jugador";
            
            // Pasar gameId para cargar desde Firebase (NO crear local)
            gameController.initMultiplayerGame(gameId, playerEmail, FIREBASE_URL);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("InazumaGo - Partida: " + gameId);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading game screen", e);
            statusLabel.setText("Error al cargar la partida");
        }
    }

    @FXML
    private void onCancel() {
        if (searching) {
            searching = false;
            if (currentSearch != null) {
                currentSearch.cancel(true);
            }
            stopTimer();
            
            // Limpiar juego creado si existe
            if (createdGameId != null) {
                firebaseRepository.deleteGame(createdGameId);
                createdGameId = null;
            }
            
            if (searchButton != null) {
                searchButton.setDisable(false);
            }
            goBackToMainMenu();
            return;
        }
        goBackToMainMenu();
    }

    private void updateReadyState() {
        searching = false;
        statusLabel.setText(currentPlayer != null
                ? "Listo para buscar partida. Pulsa 'Buscar partida'."
                : "Cargando jugador...");
        if (timeLabel != null) {
            timeLabel.setText("Tiempo de espera: 0s");
        }
        if (searchButton != null) {
            searchButton.setDisable(false);
        }
        if (cancelButton != null) {
            cancelButton.setText("Volver");
        }
    }

    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            // Obtener email del jugador
            String playerEmail = currentPlayer != null ? currentPlayer.getId() : "";
            es.iesquevedo.controller.MainMenuController mainMenuController = loader.getController();
            mainMenuController.setPlayerEmail(playerEmail);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("InazumaGo - Menú Principal");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error returning to main menu", e);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
package es.iesquevedo.ui;

import es.iesquevedo.config.AppState;
import es.iesquevedo.controller.MultiplayerGameController;
import es.iesquevedo.model.Player;
import es.iesquevedo.repository.firebase.FirebaseMainRepository;
import es.iesquevedo.service.impl.MultiplayerGameServiceImpl;
import es.iesquevedo.service.MultiplayerGameService;
import es.iesquevedo.service.impl.GameServiceImpl;
import es.iesquevedo.service.GameService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador mejorado de emparejamiento con soporte para multijugador en Firebase.
 * Permite crear o unirse a partidas multijugador desde múltiples dispositivos.
 */
public class MultiplayerMatchingController {
    private static final Logger LOGGER = Logger.getLogger(MultiplayerMatchingController.class.getName());
    private static final String FIREBASE_URL = "https://inazumago-default-rtdb.firebaseio.com";

    @FXML
    private Label statusLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Button createGameButton;

    @FXML
    private Button joinGameButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button cancelButton;

    @FXML
    private ListView<String> availableGamesListView;

    private Player currentPlayer;
    private MultiplayerGameService multiplayerService;
    private GameService gameService = new GameServiceImpl();
    private Timer timer;
    private int elapsedSeconds = 0;
    private volatile boolean searching = false;
    private String selectedGameId;

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
        FirebaseMainRepository repository = new FirebaseMainRepository(FIREBASE_URL);
        
        String token = AppState.getInstance().getAuthToken();
        if (token != null) {
            repository.setIdToken(token);
        }
        
        this.multiplayerService = new MultiplayerGameServiceImpl(repository);
        updateReadyState();
    }

    @FXML
    private void onCreateGame() {
        if (currentPlayer == null) {
            statusLabel.setText("No hay jugador autenticado");
            return;
        }

        statusLabel.setText("Creando partida multijugador...");
        createGameButton.setDisable(true);
        joinGameButton.setDisable(true);

        multiplayerService.createMultiplayerGame("Partida_" + UUID.randomUUID().toString(), currentPlayer)
            .thenAccept(gameId -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.INFO, "Partida creada: " + gameId);
                    statusLabel.setText("Partida creada. Esperando jugador...");
                    navigateToMultiplayerGame(gameId, "creador");
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error al crear partida: " + ex.getMessage());
                    createGameButton.setDisable(false);
                    joinGameButton.setDisable(false);
                });
                return null;
            });
    }

    @FXML
    private void onSearchAvailableGames() {
        if (!searching) {
            searching = true;
            statusLabel.setText("Buscando partidas disponibles...");
            searchButton.setDisable(true);
            startTimer();
            loadAvailableGames();
        }
    }

    private void loadAvailableGames() {
        multiplayerService.getAvailableGames()
            .thenAccept(gameIds -> {
                Platform.runLater(() -> {
                    availableGamesListView.getItems().clear();
                    if (gameIds.isEmpty()) {
                        statusLabel.setText("No hay partidas disponibles");
                        availableGamesListView.getItems().add("(ninguna disponible)");
                    } else {
                        statusLabel.setText("Partidas encontradas: " + gameIds.size());
                        availableGamesListView.getItems().addAll(gameIds);
                    }
                });

                // Continuar buscando cada 2 segundos
                if (searching) {
                    Timer refreshTimer = new Timer(true);
                    refreshTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (searching) {
                                loadAvailableGames();
                            }
                        }
                    }, 2000);
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error al buscar partidas: " + ex.getMessage());
                });
                return null;
            });
    }

    @FXML
    private void onJoinSelectedGame() {
        if (availableGamesListView.getSelectionModel().isEmpty()) {
            statusLabel.setText("Selecciona una partida");
            return;
        }

        String selectedGame = availableGamesListView.getSelectionModel().getSelectedItem();
        if (selectedGame == null || selectedGame.equals("(ninguna disponible)")) {
            statusLabel.setText("Selecciona una partida válida");
            return;
        }

        selectedGameId = selectedGame;
        statusLabel.setText("Uniéndote a la partida...");
        searching = false;
        stopTimer();
        joinGameButton.setDisable(true);
        searchButton.setDisable(true);

        multiplayerService.joinMultiplayerGame(selectedGameId, currentPlayer)
            .thenAccept(game -> {
                Platform.runLater(() -> {
                    LOGGER.log(Level.INFO, "Se ha unido a la partida: " + selectedGameId);
                    statusLabel.setText("¡Te has unido a la partida!");
                    
                    // Iniciar partida
                    multiplayerService.startMultiplayerGame(selectedGameId)
                        .thenAccept(v -> {
                            Platform.runLater(() -> {
                                navigateToMultiplayerGame(selectedGameId, "jugador");
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                statusLabel.setText("Error al iniciar partida: " + ex.getMessage());
                            });
                            return null;
                        });
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("Error al unirse: " + ex.getMessage());
                    joinGameButton.setDisable(false);
                    searchButton.setDisable(false);
                });
                return null;
            });
    }

    @FXML
    private void onCancel() {
        searching = false;
        stopTimer();
        goBackToMainMenu();
    }

    private void navigateToMultiplayerGame(String gameId, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MultiplayerGame.fxml"));
            Parent root = loader.load();

            MultiplayerGameController controller = loader.getController();
            
            // Obtener email del jugador - respaldo desde AppState si es necesario
            String playerEmail = null;
            if (currentPlayer != null && currentPlayer.getId() != null) {
                playerEmail = currentPlayer.getId();
            } else {
                String appStateEmail = AppState.getInstance().getCurrentUserEmail();
                playerEmail = appStateEmail != null ? appStateEmail : "Jugador";
            }
            
            if ("creador".equals(role)) {
                controller.initMultiplayerGame(gameId, playerEmail, FIREBASE_URL);
            } else {
                controller.joinMultiplayerGame(gameId, currentPlayer, FIREBASE_URL);
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 700));
            stage.setTitle("InazumaGo - Partida Multijugador: " + gameId);
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al cargar pantalla de partida multijugador", e);
            statusLabel.setText("Error al cargar la partida");
        }
    }

    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle("InazumaGo - Login");
            stage.show();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al volver al menú principal", e);
        }
    }

    private void startTimer() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    elapsedSeconds++;
                    timeLabel.setText("Tiempo de búsqueda: " + elapsedSeconds + "s");
                });
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        elapsedSeconds = 0;
    }

    private void updateReadyState() {
        createGameButton.setDisable(false);
        joinGameButton.setDisable(false);
        searchButton.setDisable(false);
        statusLabel.setText("Listo. Crea una partida o busca jugadores.");
    }
}


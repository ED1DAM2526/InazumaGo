package es.iesquevedo.controller;

import es.iesquevedo.exception.ApiError;
import es.iesquevedo.exception.MainErrorHandler;
import es.iesquevedo.service.MainService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final MainErrorHandler errorHandler = new MainErrorHandler();

    // Configurable image paths
    private static final String GAME_BOARD_PATH = "/images/game-board.png";
    private static final String BLACK_STONE_PATH = "/images/stone-black.png";
    private static final String WHITE_STONE_PATH = "/images/stone-white.png";
    private static final String RESOURCE_NOT_FOUND_PREFIX = "Recurso no encontrado: ";

    private MainService mainService;

    @FXML
    private Label salutoLabel;

    @FXML
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView gameBoardImageView;

    @FXML
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView blackStoneImageView;

    @FXML
    @SuppressWarnings("FieldCanBeLocal")
    private ImageView whiteStoneImageView;

    public MainController() {
        // Constructor sin parámetros necesario para FXML
        this.mainService = null;
    }

    public void setService(MainService mainService) {
        this.mainService = mainService;
        // Reinicializar después de establecer el servicio
        initialize();
    }

    @FXML
    public void initialize() {
        // Este método se llama automáticamente después de que FXML carga los componentes
        setupGreeting();
        
        // Cargar imagen del tablero y fichas
        loadGameBoard();
        loadStones();
    }
    
    private void setupGreeting() {
        if (mainService != null && salutoLabel != null) {
            try {
                String saludo = mainService.greet();
                handleGreetingSuccess(saludo);
            } catch (RuntimeException e) {
                handleGreetingError(e);
            }
        } else if (salutoLabel != null) {
            salutoLabel.setText("Servicio no disponible");
        }
    }
    
    private void handleGreetingSuccess(String saludo) {
        salutoLabel.setText(saludo);
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(String.format("Saludo establecido: %s", saludo));
        }
    }
    
    private void handleGreetingError(RuntimeException e) {
        ApiError apiError = errorHandler.toApiError(e);
        LOGGER.log(Level.SEVERE, "Error al obtener saludo del servicio", e);
        salutoLabel.setText(formatApiError(apiError));
    }
    
    private void loadGameBoard() {
        if (gameBoardImageView != null) {
            loadImage(gameBoardImageView, GAME_BOARD_PATH, "Tablero cargado correctamente", "No se pudo cargar la imagen del tablero");
        }
    }

    private void loadStones() {
        loadStone(blackStoneImageView, BLACK_STONE_PATH, 100, 100);
        loadStone(whiteStoneImageView, WHITE_STONE_PATH, 200, 200);
    }
    
    private void loadImage(ImageView imageView, String path, String successMessage, String errorMessage) {
        try {
            var resourceStream = getClass().getResourceAsStream(path);
            if (resourceStream != null) {
                Image image = new Image(resourceStream);
                imageView.setImage(image);
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info(successMessage);
                }
            } else {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, String.format("%s%s", RESOURCE_NOT_FOUND_PREFIX, path));
                }
            }
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, errorMessage, e);
            }
            // El ImageView se queda vacío si no encuentra la imagen
        }
    }
    
    private void loadStone(ImageView imageView, String path, double x, double y) {
        if (imageView == null) return;
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                logResourceNotFound(path);
                return;
            }
            Image stone = new Image(stream);
            imageView.setImage(stone);
            imageView.setFitWidth(40);
            imageView.setFitHeight(40);
            // Ejemplo: posición sobre el tablero (ajustar según el layout real)
            imageView.setLayoutX(x);
            imageView.setLayoutY(y);
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "No se pudo cargar la ficha", e);
            }
        }
    }
    
    private void logResourceNotFound(String path) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            LOGGER.log(Level.WARNING, String.format("%s%s", RESOURCE_NOT_FOUND_PREFIX, path));
        }
    }

    public String greet() {
        return mainService != null ? mainService.greet() : "Servicio no disponible";
    }

    static String formatApiError(ApiError apiError) {
        return String.format("Error [%s]: %s", apiError.getCode(), apiError.getMessage());
    }
}

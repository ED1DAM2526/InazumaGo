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

    private MainService mainService;

    @FXML
    private Label salutoLabel;

    @FXML
    private ImageView gameBoardImageView;

    @FXML
    private ImageView blackStoneImageView;

    @FXML
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
        if (mainService != null && salutoLabel != null) {
            try {
                String saludo = mainService.greet();
                salutoLabel.setText(saludo);
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info(String.format("Saludo establecido: %s", saludo));
                }
            } catch (RuntimeException e) {
                ApiError apiError = errorHandler.toApiError(e);
                LOGGER.log(Level.SEVERE, "Error al obtener saludo del servicio", e);
                salutoLabel.setText(formatApiError(apiError));
            }
        } else if (salutoLabel != null) {
            salutoLabel.setText("Servicio no disponible");
        }
        
        // Cargar imagen del tablero y fichas
        loadGameBoard();
        loadStones();
    }
    
    private void loadGameBoard() {
        if (gameBoardImageView != null) {
            try {
                Image image = new Image(getClass().getResourceAsStream(GAME_BOARD_PATH));
                gameBoardImageView.setImage(image);
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Tablero cargado correctamente");
                }
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "No se pudo cargar la imagen del tablero", e);
                }
                // El ImageView se queda vacío si no encuentra la imagen
            }
        }
    }

    private void loadStones() {
        // Cargar ficha negra
        if (blackStoneImageView != null) {
            try {
                Image blackStone = new Image(getClass().getResourceAsStream(BLACK_STONE_PATH));
                blackStoneImageView.setImage(blackStone);
                blackStoneImageView.setFitWidth(40);
                blackStoneImageView.setFitHeight(40);
                // Ejemplo: posición sobre el tablero (ajustar según el layout real)
                blackStoneImageView.setLayoutX(100);
                blackStoneImageView.setLayoutY(100);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "No se pudo cargar la ficha negra", e);
                }
            }
        }
        // Cargar ficha blanca
        if (whiteStoneImageView != null) {
            try {
                Image whiteStone = new Image(getClass().getResourceAsStream(WHITE_STONE_PATH));
                whiteStoneImageView.setImage(whiteStone);
                whiteStoneImageView.setFitWidth(40);
                whiteStoneImageView.setFitHeight(40);
                // Ejemplo: posición sobre el tablero (ajustar según el layout real)
                whiteStoneImageView.setLayoutX(200);
                whiteStoneImageView.setLayoutY(200);
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "No se pudo cargar la ficha blanca", e);
                }
            }
        }
    }

    public String greet() {
        return mainService != null ? mainService.greet() : "Servicio no disponible";
    }

    static String formatApiError(ApiError apiError) {
        return String.format("Error [%s]: %s", apiError.getCode(), apiError.getMessage());
    }

    public void setSalutoLabel(Label label) {
        this.salutoLabel = label;
    }
}

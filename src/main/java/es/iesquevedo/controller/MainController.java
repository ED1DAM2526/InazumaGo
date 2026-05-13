package es.iesquevedo.controller;

import es.iesquevedo.exception.ApiError;
import es.iesquevedo.exception.MainErrorHandler;
import es.iesquevedo.service.MainService;
import es.iesquevedo.dto.Position;
import es.iesquevedo.service.impl.MainServiceImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private final MainErrorHandler errorHandler = new MainErrorHandler();

    @FXML
    private Button btnNewGame;

    @FXML
    private GridPane boardGrid;

    @FXML
    private Label lblStatus;

    private MainService mainService;

    public MainController() {
        // No instanciamos por defecto el servicio para que los tests puedan verificar comportamiento con null
        this.mainService = null;
    }

    public void setService(MainService mainService) {
        this.mainService = mainService;
        // NOTA: no llamamos a initUIIfAvailable() aquí para evitar efectos secundarios (ej. llamadas a greet())
    }

    @FXML
    public void initialize() {
        // Llamado por FXML loader cuando los nodos están disponibles
        initUIIfAvailable();
    }

    private void initUIIfAvailable() {
        if (mainService == null) {
            // Mostrar estado por defecto en UI si existe
            if (lblStatus != null) lblStatus.setText("Servicio no disponible");
            return;
        }

        try {
            // inicializar tablero visual si el GridPane ya contiene celdas
            if (boardGrid != null) renderBoard();
            String saludo = mainService.greet();
            if (lblStatus != null) lblStatus.setText(saludo);
            LOGGER.info("Saludo establecido: " + saludo);
        } catch (RuntimeException e) {
            ApiError apiError = errorHandler.toApiError(e);
            LOGGER.log(Level.SEVERE, "Error al obtener saludo del servicio", e);
            if (lblStatus != null) lblStatus.setText(formatApiError(apiError));
        }
    }

    @FXML
    public void onNewGame(ActionEvent event) {
        if (mainService != null) mainService.startNewGame();
        if (boardGrid != null) renderBoard();
        if (lblStatus != null) lblStatus.setText("Turno: " + (mainService != null ? mainService.getCurrentPlayer() : " - "));
    }

    @FXML
    public void onCellClick(MouseEvent event) {
        Node source = (Node) event.getSource();
        Object rowObj = source.getProperties().get("row");
        Object colObj = source.getProperties().get("col");
        if (rowObj instanceof Integer && colObj instanceof Integer && mainService != null) {
            int row = (Integer) rowObj;
            int col = (Integer) colObj;
            Position pos = new Position(row, col);
            try {
                boolean moved = mainService.makeMove(pos);
                if (moved) {
                    if (boardGrid != null) renderBoard();
                    if (mainService.getWinner().isPresent()) {
                        if (lblStatus != null) lblStatus.setText("Ganador: " + mainService.getWinner().get());
                    } else {
                        if (lblStatus != null) lblStatus.setText("Turno: " + mainService.getCurrentPlayer());
                    }
                }
            } catch (RuntimeException ex) {
                // mostrar mensaje simple en label
                if (lblStatus != null) lblStatus.setText(ex.getMessage());
            }
        }
    }

    private void renderBoard() {
        if (boardGrid == null) return;
        var board = mainService.getBoard();
        if (board == null) return;
        int size = board.length;

        // Si no hay children, crearlos
        if (boardGrid.getChildren().isEmpty()) {
            boardGrid.getChildren().clear();
            boardGrid.getRowConstraints().clear();
            boardGrid.getColumnConstraints().clear();
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    StackPane cell = new StackPane();
                    cell.setPrefSize(80, 80);
                    cell.getStyleClass().add("board-cell");
                    cell.getProperties().put("row", r);
                    cell.getProperties().put("col", c);
                    cell.setOnMouseClicked(this::onCellClick);
                    boardGrid.add(cell, c, r);
                }
            }
        }

        // Actualizar texto en cada celda
        for (Node node : boardGrid.getChildren()) {
            Integer col = GridPane.getColumnIndex(node);
            Integer row = GridPane.getRowIndex(node);
            int r = row == null ? 0 : row;
            int c = col == null ? 0 : col;
            StackPane cell = (StackPane) node;
            cell.getChildren().clear();
            String value = board[r][c];
            if (value != null && !value.isEmpty()) {
                Label lbl = new Label(value);
                lbl.setFont(new Font(32));
                cell.getChildren().add(lbl);
            }
        }
    }

    public String greet() {
        return mainService != null ? mainService.greet() : "Servicio no disponible";
    }

    static String formatApiError(ApiError apiError) {
        return "Error [" + apiError.getCode() + "]: " + apiError.getMessage();
    }

    public void setSalutoLabel(Label label) {
        this.lblStatus = label;
    }
}

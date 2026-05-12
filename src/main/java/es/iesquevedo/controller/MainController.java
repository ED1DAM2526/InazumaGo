package es.iesquevedo.controller;

import es.iesquevedo.service.MainService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private final MainService mainService;

    @FXML
    private Label salutoLabel;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        salutoLabel.setText(mainService.status());
    }

    public String status() {
        return mainService.status();
    }
}

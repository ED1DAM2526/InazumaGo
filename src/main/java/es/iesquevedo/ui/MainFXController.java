package es.iesquevedo.ui;

import es.iesquevedo.service.auth.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainFXController {

    @FXML private Label saludoLabel;
    @FXML private Label tokenLabel;

    private AuthService authService;

    public void init(AuthService authService) {
        this.authService = authService;
        saludoLabel.setText("¡Bienvenido a InazumaGo!");
        tokenLabel.setText("Token: " + authService.getToken());
    }

    @FXML
    private void handleLogout() {
        authService.logout();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Stage stage = (Stage) saludoLabel.getScene().getWindow();
            stage.setScene(new Scene(loader.load(), 350, 250));
            stage.setTitle("InazumaGo — Login");

            LoginController loginCtrl = loader.getController();
            loginCtrl.setAuthService(authService);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
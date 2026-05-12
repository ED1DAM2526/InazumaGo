package es.iesquevedo;

import es.iesquevedo.service.auth.AuthService;
import es.iesquevedo.service.auth.impl.AuthServiceMock;
import es.iesquevedo.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Crear el AuthService (mock para desarrollo)
        AuthService authService = new AuthServiceMock();

        // Cargar la pantalla de login
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );
        primaryStage.setScene(new Scene(loader.load(), 350, 250));
        primaryStage.setTitle("InazumaGo — Login");

        // Inyectar el AuthService en el controlador
        LoginController loginCtrl = loader.getController();
        loginCtrl.setAuthService(authService);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
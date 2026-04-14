package es.iesquevedo;

import es.iesquevedo.ui.UIAdapter;
import es.iesquevedo.controller.MainController;
import es.iesquevedo.service.impl.MainServiceImpl;
import es.iesquevedo.repository.inmemory.InMemoryMainRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Configura la app como en Main.java
        var repository = new InMemoryMainRepository();
        var service = new MainServiceImpl(repository);
        var controller = new MainController(service);
        var ui = new UIAdapter(controller);

        // Crea la UI
        Label label = new Label(ui.greet());
        label.setId("greetingLabel"); // Para que el test lo encuentre
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 200);

        primaryStage.setTitle("InazumaGo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

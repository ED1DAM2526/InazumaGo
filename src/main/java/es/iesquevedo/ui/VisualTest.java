package es.iesquevedo.ui;

import es.iesquevedo.MainApp; // Asume que tienes una clase MainApp que extiende Application
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Caso de prueba visual para verificar:
 * - Arranque correcto de la ventana JavaFX.
 * - Presencia y contenido del label de saludo.
 */
public class VisualTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        // Lanza la aplicación JavaFX (debes tener MainApp configurada)
        new MainApp().start(stage);
    }

    /**
     * TC-Visual-001: Verificar que la ventana se arranca y muestra correctamente.
     */
    @Test
    void testWindowStartup(FxRobot robot) {
        // Verifica que la ventana principal esté visible
        Stage primaryStage = robot.lookup(".root").queryAs(Stage.class); // Asume que la raíz es la Stage
        assertNotNull(primaryStage, "La ventana principal no debe ser nula");
        assertTrue(primaryStage.isShowing(), "La ventana debe estar visible");

        // Verifica que el título de la ventana sea correcto (ajusta según tu app)
        assertEquals("InazumaGo", primaryStage.getTitle(), "El título de la ventana debe ser 'InazumaGo'");
    }

    /**
     * TC-Visual-002: Verificar presencia y contenido del label de saludo.
     */
    @Test
    void testLabelPresence(FxRobot robot) {
        // Busca el label por ID o texto (ajusta el selector según tu FXML o código)
        Label greetingLabel = robot.lookup("#greetingLabel").queryAs(Label.class); // Asume ID "greetingLabel"
        assertNotNull(greetingLabel, "El label de saludo debe estar presente");

        // Verifica que el texto contenga el saludo esperado
        String text = greetingLabel.getText();
        assertNotNull(text, "El texto del label no debe ser nulo");
        assertTrue(text.contains("Hello"), "El label debe contener 'Hello'. Texto actual: " + text);
        assertFalse(text.trim().isEmpty(), "El label no debe estar vacío");
    }
}

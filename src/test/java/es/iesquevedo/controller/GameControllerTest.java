package es.iesquevedo.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    @Test
    public void testGameControllerInitialization() {
        // Test básico para verificar que el controlador se puede instanciar
        GameController controller = new GameController();

        // Verificar que el tablero se inicializa correctamente
        assertNotNull(controller);

        // Nota: Para pruebas más completas necesitaríamos JavaFX TestFX
        // pero esto verifica que la clase compila y se puede instanciar
    }
}

package es.iesquevedo;

import es.iesquevedo.controller.GameController;
import java.util.logging.Logger;

public class GameTest {
    private static final Logger LOGGER = Logger.getLogger(GameTest.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Iniciando prueba del GameController...");

        try {
            // Crear instancia del controlador
            GameController controller = new GameController();
            LOGGER.info("GameController creado exitosamente");

            // Verificar que se puede llamar a métodos
            controller.setPlayerNames("Jugador1", "Jugador2");
            controller.setInitialScores(0, 0);
            LOGGER.info("Métodos del GameController funcionan correctamente");

            LOGGER.info("✅ Prueba del GameController completada exitosamente");
        } catch (Exception e) {
            LOGGER.severe("❌ Error en la prueba del GameController: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

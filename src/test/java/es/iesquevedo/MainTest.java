package es.iesquevedo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void mainOutputsGreeting() {
        // Capturamos logs de java.util.logging para comprobar la salida de Main.main
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamHandler handler = new StreamHandler(new PrintStream(baos), new SimpleFormatter());
        rootLogger.addHandler(handler);
        try {
            Main.main(new String[]{});
            // Forzar flush para que el handler escriba el contenido al stream
            handler.flush();
            String out = baos.toString();
            assertTrue(out.contains("Hello"), "Salida esperada no encontrada. salida: " + out);
        } finally {
            rootLogger.removeHandler(handler);
            handler.close();
        }
    }
}

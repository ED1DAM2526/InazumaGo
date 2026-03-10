package es.iesquevedo;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void mainOutputsGreeting() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        String out = baos.toString();
        assertTrue(out.contains("Hello and welcome!"), "Salida esperada no encontrada. salida: " + out);
    }
}

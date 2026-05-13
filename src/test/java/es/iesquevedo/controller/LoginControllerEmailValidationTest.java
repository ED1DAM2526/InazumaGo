package es.iesquevedo.controller;

import es.iesquevedo.util.EmailUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerEmailValidationTest {

    @Test
    public void testValidEmails() {
        assertTrue(EmailUtils.isValidEmail("usuario@ejemplo.com"));
        assertTrue(EmailUtils.isValidEmail("u.ser+tag-123@sub.dominio.org"));
        assertTrue(EmailUtils.isValidEmail("nombre.apellido@dominio.co"));
    }

    @Test
    public void testInvalidEmails() {
        assertFalse(EmailUtils.isValidEmail("sinArroba.ejemplo.com"));
        assertFalse(EmailUtils.isValidEmail("mal@dominio")); // sin TLD
        assertFalse(EmailUtils.isValidEmail("mal@.com"));
        assertFalse(EmailUtils.isValidEmail(" "));
        assertFalse(EmailUtils.isValidEmail(null));
    }
}



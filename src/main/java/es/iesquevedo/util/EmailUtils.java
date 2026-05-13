package es.iesquevedo.util;

public final class EmailUtils {
    private EmailUtils() {}

    /**
     * Valida el formato básico del email (asegura presencia de '@' y dominio con TLD).
     * No pretende ser una validación RFC completa, sino una comprobación práctica.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        email = email.trim();
        // Regex simple y efectivo que exige: usuario@dominio.tld (TLD >= 2 caracteres)
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}


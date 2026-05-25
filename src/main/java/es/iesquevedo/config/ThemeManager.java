package es.iesquevedo.config;

import javafx.scene.Scene;
import javafx.scene.layout.Region;

/**
 * Gestor de temas para la aplicación.
 * Permite cambiar colores de fondo dinámicamente.
 */
public class ThemeManager {
    
    public enum Theme {
        DEFAULT("Azul (Predeterminado)", "-fx-background-color: linear-gradient(to right, #1a1a2e, #16213e);"),
        PINK_WHITE("Rosa/Blanco", "-fx-background-color: linear-gradient(to right, #ffb3d9, #ffffff);"),
        BLACK_WHITE("Blanco/Negro", "-fx-background-color: linear-gradient(to right, #f5f5f5, #1a1a1a);"),
        BROWN_WHITE("Marrón/Blanco", "-fx-background-color: linear-gradient(to right, #d4a574, #fffbf0);");
        
        private final String displayName;
        private final String backgroundStyle;
        
        Theme(String displayName, String backgroundStyle) {
            this.displayName = displayName;
            this.backgroundStyle = backgroundStyle;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getBackgroundStyle() {
            return backgroundStyle;
        }
    }
    
    private static Theme currentTheme = Theme.DEFAULT;
    
    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }
    
    public static Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public static void applyThemeToScene(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle(currentTheme.getBackgroundStyle());
        }
    }
    
    public static void applyThemeToRegion(Region region) {
        if (region != null) {
            region.setStyle(currentTheme.getBackgroundStyle());
        }
    }
    
    public static String getBackgroundStyle() {
        return currentTheme.getBackgroundStyle();
    }
}

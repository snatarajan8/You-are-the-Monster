/**
 * Plain entry point for the shaded ("fat") jar. A class that extends
 * javafx.application.Application cannot be the Main-Class of a jar that bundles
 * the JavaFX modules on the classpath, so the manifest points here instead.
 */
public class Launcher {
    public static void main(String[] args) {
        Game.main(args);
    }
}

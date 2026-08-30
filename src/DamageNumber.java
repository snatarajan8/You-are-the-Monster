import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class DamageNumber {
    private Text text;
    private double x;
    private double y;
    private double velocityY;
    private int lifetime;
    private static final int MAX_LIFETIME = 30;
    private static final double GRAVITY = 0.5;

    public DamageNumber(double x, double y, int damage, Color color) {
        this.x = x;
        this.y = y;
        this.velocityY = -3;
        this.lifetime = MAX_LIFETIME;
        
        text = new Text(String.valueOf(damage));
        text.setFill(color);
        text.setFont(Font.font("Monospace", 16));
        text.setX(x);
        text.setY(y);
    }

    public boolean update() {
        lifetime--;
        y += velocityY;
        velocityY += GRAVITY;
        
        text.setX(x);
        text.setY(y);
        
        // Fade out
        double opacity = (double) lifetime / MAX_LIFETIME;
        text.setOpacity(opacity);
        
        return lifetime > 0;
    }

    public Text getText() {
        return text;
    }
}

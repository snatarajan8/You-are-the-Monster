import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

public class HUD {
    private Pane pane;
    private Rectangle healthBar;
    private Rectangle healthBarBackground;
    private Text healthText;
    private static final int HUD_X = 10;
    private static final int HUD_Y = 10;
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;

    public HUD() {
        pane = new Pane();
        
        // Health bar background
        healthBarBackground = new Rectangle(HUD_X, HUD_Y, BAR_WIDTH, BAR_HEIGHT);
        healthBarBackground.setFill(Color.DARK_GRAY);
        healthBarBackground.setStroke(Color.BLACK);
        healthBarBackground.setStrokeWidth(2);
        
        // Health bar
        healthBar = new Rectangle(HUD_X, HUD_Y, BAR_WIDTH, BAR_HEIGHT);
        healthBar.setFill(Color.RED);
        
        // Health text
        healthText = new Text(HUD_X + BAR_WIDTH + 10, HUD_Y + 15, "100/100");
        healthText.setFill(Color.WHITE);
        healthText.setFont(Font.font("Monospace", 14));
        
        pane.getChildren().addAll(healthBarBackground, healthBar, healthText);
    }

    public void update(int currentHealth, int maxHealth) {
        // Update health bar width
        double healthPercentage = (double) currentHealth / maxHealth;
        healthBar.setWidth(BAR_WIDTH * healthPercentage);
        
        // Change color based on health
        if (healthPercentage > 0.6) {
            healthBar.setFill(Color.GREEN);
        } else if (healthPercentage > 0.3) {
            healthBar.setFill(Color.YELLOW);
        } else {
            healthBar.setFill(Color.RED);
        }
        
        // Update text
        healthText.setText(currentHealth + "/" + maxHealth);
    }

    public Pane getPane() {
        return pane;
    }
}

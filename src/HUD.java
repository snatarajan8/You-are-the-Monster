import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class HUD {

    private final Pane pane = new Pane();
    private final Rectangle healthBar;
    private final Text healthText;
    private final Text levelText;
    private final Text scoreText;
    private static final int BAR_WIDTH = 220;
    private static final int BAR_HEIGHT = 18;

    public HUD() {
        Rectangle bg = new Rectangle(12, 12, BAR_WIDTH, BAR_HEIGHT);
        bg.setFill(Color.web("#00000088"));
        bg.setStroke(Color.web("#000000"));
        bg.setStrokeWidth(2);

        healthBar = new Rectangle(12, 12, BAR_WIDTH, BAR_HEIGHT);
        healthBar.setFill(Color.web("#3fbf5f"));

        healthText = label(12 + BAR_WIDTH + 10, 26, 13, Color.WHITE);
        levelText = label(12, 52, 13, Color.web("#c8c8d8"));
        scoreText = label(12, 72, 13, Color.web("#e0c060"));

        pane.getChildren().addAll(bg, healthBar, healthText, levelText, scoreText);
        pane.setMouseTransparent(true);
    }

    private Text label(double x, double y, int size, Color color) {
        Text t = new Text(x, y, "");
        t.setFill(color);
        t.setFont(Font.font("Monospaced", size));
        return t;
    }

    public void update(int health, int maxHealth, String levelName, int levelIndex, int totalLevels, int score) {
        double pct = maxHealth <= 0 ? 0 : Math.max(0, (double) health / maxHealth);
        healthBar.setWidth(BAR_WIDTH * pct);
        healthBar.setFill(pct > 0.6 ? Color.web("#3fbf5f") : pct > 0.3 ? Color.web("#d4b02a") : Color.web("#c8402a"));
        healthText.setText(Math.max(0, health) + " / " + maxHealth);
        levelText.setText("Level " + (levelIndex + 1) + "/" + totalLevels + "  -  " + levelName);
        scoreText.setText("Score: " + score);
    }

    public Pane getPane() {
        return pane;
    }
}

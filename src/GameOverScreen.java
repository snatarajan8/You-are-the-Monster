import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameOverScreen {
    private Pane pane;
    private Rectangle overlay;
    private Text gameOverText;
    private Text retryText;
    private Text menuText;

    public GameOverScreen(int width, int height) {
        pane = new Pane();
        
        // Semi-transparent overlay
        overlay = new Rectangle(0, 0, width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.8));
        
        // Game over text
        gameOverText = new Text(width/2 - 180, height/2 - 60, "GAME OVER");
        gameOverText.setFill(Color.RED);
        gameOverText.setFont(Font.font("Monospace", 64));
        
        // Retry text
        retryText = new Text(width/2 - 150, height/2 + 20, "Press ENTER to retry");
        retryText.setFill(Color.LIGHTGRAY);
        retryText.setFont(Font.font("Monospace", 20));
        
        // Menu text
        menuText = new Text(width/2 - 150, height/2 + 60, "Press ESC for menu");
        menuText.setFill(Color.GRAY);
        menuText.setFont(Font.font("Monospace", 16));
        
        pane.getChildren().addAll(overlay, gameOverText, retryText, menuText);
        pane.setVisible(false);
    }

    public void show() {
        pane.setVisible(true);
    }

    public void hide() {
        pane.setVisible(false);
    }

    public Pane getPane() {
        return pane;
    }
}

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PauseMenu {
    private Pane pane;
    private Rectangle overlay;
    private Text pauseText;
    private Text resumeText;
    private Text quitText;
    private Text controlsText;

    public PauseMenu(int width, int height) {
        pane = new Pane();
        
        // Semi-transparent overlay
        overlay = new Rectangle(0, 0, width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // Pause text
        pauseText = new Text(width/2 - 100, height/2 - 60, "PAUSED");
        pauseText.setFill(Color.WHITE);
        pauseText.setFont(Font.font("Monospace", 48));
        
        // Resume text
        resumeText = new Text(width/2 - 150, height/2, "Press ENTER or ESC to resume");
        resumeText.setFill(Color.LIGHTGRAY);
        resumeText.setFont(Font.font("Monospace", 18));
        
        // Controls text
        controlsText = new Text(width/2 - 200, height/2 + 50, 
            "Arrow Keys / WASD = Move    UP = Jump    Z / Click = Attack");
        controlsText.setFill(Color.GRAY);
        controlsText.setFont(Font.font("Monospace", 14));
        
        pane.getChildren().addAll(overlay, pauseText, resumeText, controlsText);
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

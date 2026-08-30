import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class VictoryScreen {
    private Pane pane;
    private Rectangle overlay;
    private Text victoryText;
    private Text levelText;
    private Text continueText;
    private Text scoreText;

    public VictoryScreen(int width, int height) {
        pane = new Pane();
        
        // Semi-transparent overlay
        overlay = new Rectangle(0, 0, width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.8));
        
        // Victory text
        victoryText = new Text(width/2 - 200, height/2 - 80, "LEVEL COMPLETE!");
        victoryText.setFill(Color.GREEN);
        victoryText.setFont(Font.font("Monospace", 48));
        
        // Level text
        levelText = new Text(width/2 - 100, height/2 - 20, "Area Complete");
        levelText.setFill(Color.WHITE);
        levelText.setFont(Font.font("Monospace", 24));
        
        // Score text
        scoreText = new Text(width/2 - 100, height/2 + 30, "Score: 0");
        scoreText.setFill(Color.YELLOW);
        scoreText.setFont(Font.font("Monospace", 18));
        
        // Continue text
        continueText = new Text(width/2 - 150, height/2 + 80, "Press ENTER to continue");
        continueText.setFill(Color.LIGHTGRAY);
        continueText.setFont(Font.font("Monospace", 16));
        
        pane.getChildren().addAll(overlay, victoryText, levelText, scoreText, continueText);
        pane.setVisible(false);
    }

    public void show(String levelName, int score) {
        levelText.setText(levelName);
        scoreText.setText("Score: " + score);
        pane.setVisible(true);
    }

    public void hide() {
        pane.setVisible(false);
    }

    public Pane getPane() {
        return pane;
    }
}

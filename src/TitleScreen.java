import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class TitleScreen {
    private Pane pane;
    private Text titleText;
    private Text subtitleText;
    private Text startText;
    private Text difficultyText;
    private Text controlsText;
    private Rectangle background;
    private int selectedDifficulty;
    private static final String[] DIFFICULTIES = {"Easy", "Normal", "Hard", "Nightmare"};
    private static final Color[] DIFFICULTY_COLORS = {Color.GREEN, Color.WHITE, Color.ORANGE, Color.RED};

    public TitleScreen(int width, int height) {
        pane = new Pane();
        selectedDifficulty = 1; // Default to Normal
        
        // Background
        background = new Rectangle(0, 0, width, height);
        background.setFill(Color.rgb(10, 10, 26));
        
        // Title
        titleText = new Text(width/2 - 250, height/2 - 100, "YOU ARE THE MONSTER");
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Monospace", 56));
        
        // Subtitle
        subtitleText = new Text(width/2 - 150, height/2 - 50, "A Ludum Dare 33 Game");
        subtitleText.setFill(Color.LIGHTGRAY);
        subtitleText.setFont(Font.font("Monospace", 18));
        
        // Start prompt
        startText = new Text(width/2 - 150, height/2 + 20, "Press ENTER to start");
        startText.setFill(Color.YELLOW);
        startText.setFont(Font.font("Monospace", 20));
        
        // Difficulty selection
        difficultyText = new Text(width/2 - 100, height/2 + 70, "Difficulty: " + DIFFICULTIES[selectedDifficulty]);
        difficultyText.setFill(DIFFICULTY_COLORS[selectedDifficulty]);
        difficultyText.setFont(Font.font("Monospace", 18));
        
        // Controls
        controlsText = new Text(width/2 - 250, height/2 + 120, 
            "Controls: Arrow Keys = Move, UP = Jump, Click = Attack, ESC = Pause");
        controlsText.setFill(Color.GRAY);
        controlsText.setFont(Font.font("Monospace", 14));
        
        pane.getChildren().addAll(background, titleText, subtitleText, startText, difficultyText, controlsText);
    }

    public void updateDifficulty(int direction) {
        selectedDifficulty += direction;
        if (selectedDifficulty < 0) selectedDifficulty = 3;
        if (selectedDifficulty > 3) selectedDifficulty = 0;
        
        difficultyText.setText("Difficulty: " + DIFFICULTIES[selectedDifficulty]);
        difficultyText.setFill(DIFFICULTY_COLORS[selectedDifficulty]);
    }

    public int getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public Pane getPane() {
        return pane;
    }

    public void setVisible(boolean visible) {
        pane.setVisible(visible);
    }
}

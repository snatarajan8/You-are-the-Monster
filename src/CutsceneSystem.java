import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class CutsceneSystem {
    private Pane pane;
    private Rectangle overlay;
    private Text speakerText;
    private Text dialogueText;
    private Text continueText;
    private List<List<CutsceneData>> cutscenes;
    private int currentCutsceneIndex;
    private int currentLineIndex;
    private boolean active;

    public static class CutsceneData {
        public String speaker;
        public String dialogue;
        public String portraitPath;

        public CutsceneData(String speaker, String dialogue, String portraitPath) {
            this.speaker = speaker;
            this.dialogue = dialogue;
            this.portraitPath = portraitPath;
        }
    }

    public CutsceneSystem(int width, int height) {
        pane = new Pane();
        cutscenes = new ArrayList<>();
        currentCutsceneIndex = 0;
        currentLineIndex = 0;
        active = false;

        // Semi-transparent overlay
        overlay = new Rectangle(0, 0, width, height);
        overlay.setFill(Color.rgb(0, 0, 0, 0.9));

        // Speaker text
        speakerText = new Text(100, height - 150, "");
        speakerText.setFill(Color.YELLOW);
        speakerText.setFont(Font.font("Monospace", 24));

        // Dialogue text
        dialogueText = new Text(100, height - 100, "");
        dialogueText.setFill(Color.WHITE);
        dialogueText.setFont(Font.font("Monospace", 18));
        dialogueText.setWrappingWidth(width - 200);

        // Continue prompt
        continueText = new Text(width - 200, height - 50, "Press ENTER to continue");
        continueText.setFill(Color.GRAY);
        continueText.setFont(Font.font("Monospace", 14));

        pane.getChildren().addAll(overlay, speakerText, dialogueText, continueText);
        pane.setVisible(false);

        initializeCutscenes();
    }

    private void initializeCutscenes() {
        // Opening cutscene
        List<CutsceneData> opening = new ArrayList<>();
        opening.add(new CutsceneData("Narrator", 
            "In a world where humans fear the monster within...", ""));
        opening.add(new CutsceneData("Narrator", 
            "You awaken in the Dark Forest, cursed by the Vampire Lord.", ""));
        opening.add(new CutsceneData("Narrator", 
            "Your only hope is to defeat him and break the curse.", ""));
        opening.add(new CutsceneData("Narrator", 
            "But be warned - you ARE the monster now.", ""));
        cutscenes.add(opening);

        // Area transitions
        List<CutsceneData> area2 = new ArrayList<>();
        area2.add(new CutsceneData("Narrator", 
            "You emerge from the forest into an abandoned village.", ""));
        area2.add(new CutsceneData("Narrator", 
            "The villagers fled long ago, but their guards remain...", ""));
        cutscenes.add(area2);

        List<CutsceneData> area3 = new ArrayList<>();
        area3.add(new CutsceneData("Narrator", 
            "The graveyard rises before you, filled with restless dead.", ""));
        area3.add(new CutsceneData("Narrator", 
            "The Skeleton King awaits in the depths...", ""));
        cutscenes.add(area3);

        List<CutsceneData> area4 = new ArrayList<>();
        area4.add(new CutsceneData("Narrator", 
            "Dark caves stretch before you, echoing with strange sounds.", ""));
        area4.add(new CutsceneData("Narrator", 
            "Something ancient lurks in the darkness...", ""));
        cutscenes.add(area4);

        List<CutsceneData> area5 = new ArrayList<>();
        area5.add(new CutsceneData("Narrator", 
            "The castle walls tower above you.", ""));
        area5.add(new CutsceneData("Narrator", 
            "Only the strongest knights guard the Vampire Lord...", ""));
        cutscenes.add(area5);

        List<CutsceneData> area6 = new ArrayList<>();
        area6.add(new CutsceneData("Narrator", 
            "You enter the throne room. The Vampire Lord awaits.", ""));
        area6.add(new CutsceneData("Vampire Lord", 
            "So, the cursed one finally arrives...", ""));
        area6.add(new CutsceneData("Vampire Lord", 
            "You think you can defeat me? You ARE the monster!", ""));
        cutscenes.add(area6);

        // Ending cutscene
        List<CutsceneData> ending = new ArrayList<>();
        ending.add(new CutsceneData("Narrator", 
            "The Vampire Lord falls. The curse is broken.", ""));
        ending.add(new CutsceneData("Narrator", 
            "But at what cost? The monster within still lurks...", ""));
        ending.add(new CutsceneData("Narrator", 
            "THE END", ""));
        cutscenes.add(ending);
    }

    public void startCutscene(int index) {
        if (index < 0 || index >= cutscenes.size()) return;
        
        currentCutsceneIndex = index;
        currentLineIndex = 0;
        active = true;
        showCurrentLine();
        pane.setVisible(true);
    }

    private void showCurrentLine() {
        List<CutsceneData> currentCutscene = cutscenes.get(currentCutsceneIndex);
        if (currentLineIndex < currentCutscene.size()) {
            CutsceneData line = currentCutscene.get(currentLineIndex);
            speakerText.setText(line.speaker);
            dialogueText.setText(line.dialogue);
        }
    }

    public boolean advance() {
        if (!active) return false;
        
        List<CutsceneData> currentCutscene = cutscenes.get(currentCutsceneIndex);
        currentLineIndex++;
        
        if (currentLineIndex >= currentCutscene.size()) {
            endCutscene();
            return true; // Cutscene ended
        }
        
        showCurrentLine();
        return false; // Cutscene continues
    }

    private void endCutscene() {
        active = false;
        pane.setVisible(false);
    }

    public boolean isActive() {
        return active;
    }

    public Pane getPane() {
        return pane;
    }
}

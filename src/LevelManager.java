import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    private final Game game;
    private final List<String> levelPaths = new ArrayList<>();
    private int currentLevelIndex = 0;
    private LevelData currentLevelData;

    public LevelManager(Game game) {
        this.game = game;
        for (int area = 1; area <= 6; area++) {
            for (int lvl = 1; lvl <= 3; lvl++) {
                levelPaths.add("levels/" + area + "_" + lvl + ".json");
            }
        }
    }

    public boolean loadLevel(int index) {
        if (index < 0 || index >= levelPaths.size()) return false;
        currentLevelIndex = index;
        currentLevelData = LevelData.load(levelPaths.get(index));
        game.buildWorld(currentLevelData);
        return true;
    }

    public boolean hasNextLevel() {
        return currentLevelIndex + 1 < levelPaths.size();
    }

    public boolean loadNextLevel() {
        return hasNextLevel() && loadLevel(currentLevelIndex + 1);
    }

    public boolean reloadCurrentLevel() {
        return loadLevel(currentLevelIndex);
    }

    /** True when this level is the first of a new area (index 0, 3, 6, ...). */
    public boolean isAreaStart() {
        return currentLevelIndex % 3 == 0;
    }

    public int getAreaNumber() {
        return currentLevelIndex / 3 + 1;
    }

    public int getCurrentLevelIndex() { return currentLevelIndex; }
    public int getTotalLevels() { return levelPaths.size(); }
    public LevelData getCurrentLevelData() { return currentLevelData; }
}

import java.util.ArrayList;
import java.util.List;

public class LevelManager {
    private Game game;
    private List<String> levelPaths;
    private int currentLevelIndex;
    private LevelData currentLevelData;

    public LevelManager(Game game) {
        this.game = game;
        this.levelPaths = new ArrayList<>();
        this.currentLevelIndex = 0;
        
        // Initialize level paths
        initializeLevelPaths();
    }

    private void initializeLevelPaths() {
        // Area 1: Dark Forest
        levelPaths.add("src/levels/1_1.json");
        levelPaths.add("src/levels/1_2.json");
        levelPaths.add("src/levels/1_3.json");
        
        // Area 2: Abandoned Village
        levelPaths.add("src/levels/2_1.json");
        levelPaths.add("src/levels/2_2.json");
        levelPaths.add("src/levels/2_3.json");
        
        // Area 3: Haunted Graveyard
        levelPaths.add("src/levels/3_1.json");
        levelPaths.add("src/levels/3_2.json");
        levelPaths.add("src/levels/3_3.json");
        
        // Area 4: Cursed Caves
        levelPaths.add("src/levels/4_1.json");
        levelPaths.add("src/levels/4_2.json");
        levelPaths.add("src/levels/4_3.json");
        
        // Area 5: Castle Walls
        levelPaths.add("src/levels/5_1.json");
        levelPaths.add("src/levels/5_2.json");
        levelPaths.add("src/levels/5_3.json");
        
        // Area 6: Throne Room
        levelPaths.add("src/levels/6_1.json");
        levelPaths.add("src/levels/6_2.json");
        levelPaths.add("src/levels/6_3.json");
    }

    public boolean loadLevel(int index) {
        if (index < 0 || index >= levelPaths.size()) {
            System.out.println("Invalid level index: " + index);
            return false;
        }
        
        currentLevelIndex = index;
        String path = levelPaths.get(index);
        
        // Try to load from file, fallback to default
        currentLevelData = LevelData.loadFromJSON(path);
        
        // Clear current level
        game.getCharacterSet().clear();
        game.getCollisionSet().clear();
        
        // Create player at start position
        Player player = game.getPlayer();
        player.getRectangle().setLocation(
            currentLevelData.getPlayerStartX(), 
            currentLevelData.getPlayerStartY()
        );
        game.addUnit(player);
        
        // Create walls
        for (LevelData.WallData wallData : currentLevelData.getWalls()) {
            Wall wall = new Wall(wallData.x, wallData.y, wallData.width, wallData.height);
            game.addUnit(wall);
        }
        
        // Create enemies
        for (LevelData.EnemyData enemyData : currentLevelData.getEnemies()) {
            Character enemy = createEnemy(enemyData);
            if (enemy != null) {
                game.addUnit(enemy);
            }
        }
        
        // Set camera level size
        game.getCamera().setLevelSize(
            currentLevelData.getLevelWidth(), 
            currentLevelData.getLevelHeight()
        );
        
        System.out.println("Loaded level " + (index + 1) + ": " + path);
        return true;
    }

    private Character createEnemy(LevelData.EnemyData data) {
        switch (data.type.toLowerCase()) {
            case "bat":
                return new EnemyBat(game, data.x, data.y, 20, 20, data.patrolDistance);
            case "werewolf":
                return new EnemyWerewolf(game, data.x, data.y, data.patrolDistance);
            case "skeleton":
                return new EnemySkeleton(game, data.x, data.y, data.patrolDistance);
            case "guard":
                return new EnemyGuard(game, data.x, data.y, data.patrolDistance);
            case "zombie":
                return new EnemyZombie(game, data.x, data.y, data.patrolDistance);
            case "spider":
                return new EnemySpider(game, data.x, data.y, data.patrolDistance);
            case "knight":
                return new EnemyKnight(game, data.x, data.y, data.patrolDistance);
            case "vampirelord":
                return new BossVampireLord(game, data.x, data.y);
            default:
                System.out.println("Unknown enemy type: " + data.type);
                return null;
        }
    }

    public boolean loadNextLevel() {
        return loadLevel(currentLevelIndex + 1);
    }

    public boolean loadPreviousLevel() {
        return loadLevel(currentLevelIndex - 1);
    }

    public int getCurrentLevelIndex() {
        return currentLevelIndex;
    }

    public int getTotalLevels() {
        return levelPaths.size();
    }

    public String getCurrentLevelName() {
        return levelPaths.get(currentLevelIndex);
    }

    public LevelData getCurrentLevelData() {
        return currentLevelData;
    }
}

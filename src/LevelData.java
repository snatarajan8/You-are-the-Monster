import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LevelData {
    private String backgroundPath;
    private int levelWidth;
    private int levelHeight;
    private List<WallData> walls;
    private List<EnemyData> enemies;
    private List<ItemData> items;
    private int playerStartX;
    private int playerStartY;
    private int exitX;
    private int exitY;

    public static class WallData {
        public int x, y, width, height;
        
        public WallData(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class EnemyData {
        public String type;
        public int x, y;
        public int patrolDistance;
        
        public EnemyData(String type, int x, int y, int patrolDistance) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.patrolDistance = patrolDistance;
        }
    }

    public static class ItemData {
        public String type;
        public int x, y;
        
        public ItemData(String type, int x, int y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public LevelData() {
        walls = new ArrayList<>();
        enemies = new ArrayList<>();
        items = new ArrayList<>();
        levelWidth = 1280;
        levelHeight = 704;
    }

    public static LevelData loadFromJSON(String filePath) {
        LevelData level = new LevelData();
        
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            
            // Parse basic level info
            level.backgroundPath = (String) jsonObject.get("background");
            level.levelWidth = ((Long) jsonObject.get("width")).intValue();
            level.levelHeight = ((Long) jsonObject.get("height")).intValue();
            
            // Parse player start
            JSONObject playerStart = (JSONObject) jsonObject.get("playerStart");
            if (playerStart != null) {
                level.playerStartX = ((Long) playerStart.get("x")).intValue();
                level.playerStartY = ((Long) playerStart.get("y")).intValue();
            }
            
            // Parse exit
            JSONObject exit = (JSONObject) jsonObject.get("exit");
            if (exit != null) {
                level.exitX = ((Long) exit.get("x")).intValue();
                level.exitY = ((Long) exit.get("y")).intValue();
            }
            
            // Parse walls
            JSONArray wallsArray = (JSONArray) jsonObject.get("walls");
            if (wallsArray != null) {
                for (Object obj : wallsArray) {
                    JSONObject wallJson = (JSONObject) obj;
                    int x = ((Long) wallJson.get("x")).intValue();
                    int y = ((Long) wallJson.get("y")).intValue();
                    int width = ((Long) wallJson.get("width")).intValue();
                    int height = ((Long) wallJson.get("height")).intValue();
                    level.walls.add(new WallData(x, y, width, height));
                }
            }
            
            // Parse enemies
            JSONArray enemiesArray = (JSONArray) jsonObject.get("enemies");
            if (enemiesArray != null) {
                for (Object obj : enemiesArray) {
                    JSONObject enemyJson = (JSONObject) obj;
                    String type = (String) enemyJson.get("type");
                    int x = ((Long) enemyJson.get("x")).intValue();
                    int y = ((Long) enemyJson.get("y")).intValue();
                    int patrol = enemyJson.containsKey("patrol") ? 
                        ((Long) enemyJson.get("patrol")).intValue() : 200;
                    level.enemies.add(new EnemyData(type, x, y, patrol));
                }
            }
            
            // Parse items
            JSONArray itemsArray = (JSONArray) jsonObject.get("items");
            if (itemsArray != null) {
                for (Object obj : itemsArray) {
                    JSONObject itemJson = (JSONObject) obj;
                    String type = (String) itemJson.get("type");
                    int x = ((Long) itemJson.get("x")).intValue();
                    int y = ((Long) itemJson.get("y")).intValue();
                    level.items.add(new ItemData(type, x, y));
                }
            }
            
        } catch (IOException | ParseException e) {
            System.out.println("Error loading level: " + e.getMessage());
            // Return default level
            level.createDefaultLevel();
        }
        
        return level;
    }

    private void createDefaultLevel() {
        levelWidth = 1280;
        levelHeight = 704;
        playerStartX = 100;
        playerStartY = 500;
        backgroundPath = "resources/background layers.png";
        
        // Ground
        walls.add(new WallData(0, 644, 1280, 40));
        
        // Left wall
        walls.add(new WallData(0, 0, 160, 288));
        walls.add(new WallData(0, 288, 96, 64));
        walls.add(new WallData(0, 352, 32, 320));
        
        // Right wall
        walls.add(new WallData(1120, 0, 160, 288));
        walls.add(new WallData(1184, 288, 96, 64));
        walls.add(new WallData(1248, 352, 32, 320));
        
        // Platforms
        walls.add(new WallData(192, 480, 256, 32));
        walls.add(new WallData(480, 608, 96, 32));
        walls.add(new WallData(608, 576, 96, 32));
        walls.add(new WallData(704, 512, 192, 32));
        
        // Add some enemies
        enemies.add(new EnemyData("bat", 400, 400, 200));
        enemies.add(new EnemyData("bat", 800, 300, 150));
        enemies.add(new EnemyData("werewolf", 600, 500, 300));
    }

    // Getters
    public String getBackgroundPath() { return backgroundPath; }
    public int getLevelWidth() { return levelWidth; }
    public int getLevelHeight() { return levelHeight; }
    public List<WallData> getWalls() { return walls; }
    public List<EnemyData> getEnemies() { return enemies; }
    public List<ItemData> getItems() { return items; }
    public int getPlayerStartX() { return playerStartX; }
    public int getPlayerStartY() { return playerStartY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
}

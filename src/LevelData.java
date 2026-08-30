import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

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
            // Read file content
            StringBuilder content = new StringBuilder();
            try (FileReader reader = new FileReader(filePath)) {
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    content.append(buffer, 0, read);
                }
            }
            
            JSONObject jsonObject = new JSONObject(content.toString());
            
            // Parse basic level info
            level.backgroundPath = jsonObject.optString("background", "resources/background layers.png");
            level.levelWidth = jsonObject.optInt("width", 1280);
            level.levelHeight = jsonObject.optInt("height", 704);
            
            // Parse player start
            JSONObject playerStart = jsonObject.optJSONObject("playerStart");
            if (playerStart != null) {
                level.playerStartX = playerStart.optInt("x", 100);
                level.playerStartY = playerStart.optInt("y", 500);
            }
            
            // Parse exit
            JSONObject exit = jsonObject.optJSONObject("exit");
            if (exit != null) {
                level.exitX = exit.optInt("x", 1200);
                level.exitY = exit.optInt("y", 500);
            }
            
            // Parse walls
            JSONArray wallsArray = jsonObject.optJSONArray("walls");
            if (wallsArray != null) {
                for (int i = 0; i < wallsArray.length(); i++) {
                    JSONObject wallJson = wallsArray.getJSONObject(i);
                    int x = wallJson.optInt("x", 0);
                    int y = wallJson.optInt("y", 0);
                    int width = wallJson.optInt("width", 100);
                    int height = wallJson.optInt("height", 100);
                    level.walls.add(new WallData(x, y, width, height));
                }
            }
            
            // Parse enemies
            JSONArray enemiesArray = jsonObject.optJSONArray("enemies");
            if (enemiesArray != null) {
                for (int i = 0; i < enemiesArray.length(); i++) {
                    JSONObject enemyJson = enemiesArray.getJSONObject(i);
                    String type = enemyJson.optString("type", "bat");
                    int x = enemyJson.optInt("x", 0);
                    int y = enemyJson.optInt("y", 0);
                    int patrol = enemyJson.optInt("patrol", 200);
                    level.enemies.add(new EnemyData(type, x, y, patrol));
                }
            }
            
            // Parse items
            JSONArray itemsArray = jsonObject.optJSONArray("items");
            if (itemsArray != null) {
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemJson = itemsArray.getJSONObject(i);
                    String type = itemJson.optString("type", "meat");
                    int x = itemJson.optInt("x", 0);
                    int y = itemJson.optInt("y", 0);
                    level.items.add(new ItemData(type, x, y));
                }
            }
            
        } catch (IOException | JSONException e) {
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

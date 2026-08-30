import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class LevelData {

    private String name = "Unknown";
    private int area = 1;
    private int levelNumber = 1;
    private String backgroundPath = "resources/background layers.png";
    private int levelWidth = 1280;
    private int levelHeight = 704;
    private final List<WallData> walls = new ArrayList<>();
    private final List<EnemyData> enemies = new ArrayList<>();
    private final List<ItemData> items = new ArrayList<>();
    private int playerStartX = 100;
    private int playerStartY = 500;
    private int exitX = 1200;
    private int exitY = 500;

    public static class WallData {
        public final int x, y, width, height;
        public WallData(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
        }
    }

    public static class EnemyData {
        public final String type;
        public final int x, y, patrolDistance;
        public EnemyData(String type, int x, int y, int patrolDistance) {
            this.type = type; this.x = x; this.y = y; this.patrolDistance = patrolDistance;
        }
    }

    public static class ItemData {
        public final String type;
        public final int x, y;
        public ItemData(String type, int x, int y) {
            this.type = type; this.x = x; this.y = y;
        }
    }

    /** Loads a level from the classpath (e.g. "levels/1_1.json"), falling back to a default arena. */
    public static LevelData load(String resourcePath) {
        LevelData level = new LevelData();
        String cp = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream in = LevelData.class.getResourceAsStream(cp)) {
            if (in == null) {
                System.err.println("Level resource not found: " + cp + " - using default arena");
                level.buildDefault();
                return level;
            }
            StringBuilder sb = new StringBuilder();
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                char[] buf = new char[2048];
                int n;
                while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);
            }
            JSONObject o = new JSONObject(sb.toString());

            level.name = o.optString("name", "Level");
            level.area = o.optInt("area", 1);
            level.levelNumber = o.optInt("levelNumber", 1);
            level.backgroundPath = o.optString("background", level.backgroundPath);
            level.levelWidth = o.optInt("width", 1280);
            level.levelHeight = o.optInt("height", 704);

            JSONObject start = o.optJSONObject("playerStart");
            if (start != null) {
                level.playerStartX = start.optInt("x", 100);
                level.playerStartY = start.optInt("y", 500);
            }
            JSONObject exit = o.optJSONObject("exit");
            if (exit != null) {
                level.exitX = exit.optInt("x", 1200);
                level.exitY = exit.optInt("y", 500);
            }

            JSONArray wallsArr = o.optJSONArray("walls");
            if (wallsArr != null) {
                for (int i = 0; i < wallsArr.length(); i++) {
                    JSONObject w = wallsArr.getJSONObject(i);
                    level.walls.add(new WallData(w.optInt("x"), w.optInt("y"),
                        w.optInt("width", 100), w.optInt("height", 32)));
                }
            }
            JSONArray enemiesArr = o.optJSONArray("enemies");
            if (enemiesArr != null) {
                for (int i = 0; i < enemiesArr.length(); i++) {
                    JSONObject e = enemiesArr.getJSONObject(i);
                    level.enemies.add(new EnemyData(e.optString("type", "bat"),
                        e.optInt("x"), e.optInt("y"), e.optInt("patrol", 200)));
                }
            }
            JSONArray itemsArr = o.optJSONArray("items");
            if (itemsArr != null) {
                for (int i = 0; i < itemsArr.length(); i++) {
                    JSONObject it = itemsArr.getJSONObject(i);
                    level.items.add(new ItemData(it.optString("type", "meat"),
                        it.optInt("x"), it.optInt("y")));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading level " + resourcePath + ": " + e.getMessage());
            level.walls.clear();
            level.enemies.clear();
            level.items.clear();
            level.buildDefault();
        }
        return level;
    }

    private void buildDefault() {
        name = "Dark Forest";
        levelWidth = 1280;
        levelHeight = 704;
        playerStartX = 100;
        playerStartY = 480;
        exitX = 1180;
        exitY = 520;
        walls.add(new WallData(0, 644, 1280, 60));
        walls.add(new WallData(0, 0, 32, 704));
        walls.add(new WallData(1248, 0, 32, 704));
        walls.add(new WallData(220, 520, 220, 28));
        walls.add(new WallData(520, 440, 180, 28));
        walls.add(new WallData(780, 520, 200, 28));
        enemies.add(new EnemyData("bat", 400, 380, 200));
        enemies.add(new EnemyData("bat", 820, 300, 150));
        enemies.add(new EnemyData("werewolf", 600, 560, 300));
        items.add(new ItemData("meat", 560, 400));
    }

    public String getName() { return name; }
    public int getArea() { return area; }
    public int getLevelNumber() { return levelNumber; }
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

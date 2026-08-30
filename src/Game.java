import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Game extends Application {

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 704;
    private static final long TICK_NS = 33_333_333L; // 30 logic ticks / second
    static final boolean DEBUG = Boolean.getBoolean("game.debug");

    // --- scene graph layers ---
    private final Pane skyLayer = new Pane();
    private final Pane bgLayer = new Pane();
    private final Pane worldLayer = new Pane();
    private final Pane uiLayer = new Pane();
    private Rectangle bgRect;
    private Image bgImage;

    // --- world state ---
    private final Set<CollisionUnit> units = new HashSet<>();
    private final Set<Character> characters = new HashSet<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final List<DamageNumber> floatingText = new ArrayList<>();
    private Player player;
    private java.awt.Rectangle exitBounds;
    private Node exitNode;
    private boolean bossLevel;
    private boolean bossDefeated;
    private int score;
    private int enemiesRemaining;

    // --- systems ---
    private Camera camera;
    private HUD hud;
    private LevelManager levelManager;
    private DifficultyManager difficultyManager;
    private AudioManager audioManager;
    private TitleScreen titleScreen;
    private PauseMenu pauseMenu;
    private GameOverScreen gameOverScreen;
    private VictoryScreen victoryScreen;
    private CutsceneSystem cutscene;

    private GameState state = GameState.MENU;
    private final Set<KeyCode> keysDown = new HashSet<>();
    private final java.util.Map<KeyCode, Long> releaseAt = new java.util.HashMap<>();
    private static final long REPEAT_GRACE_NS = 60_000_000L; // ignore release+press repeat bursts
    private int pendingCutsceneAdvanceTo = -1; // level index to load after the current cutscene
    private javafx.scene.text.Text debugText;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        camera = new Camera(0, 0, WIDTH, HEIGHT);
        levelManager = new LevelManager(this);
        difficultyManager = new DifficultyManager();
        audioManager = new AudioManager();
        hud = new HUD();

        Rectangle sky = new Rectangle(WIDTH, HEIGHT, Color.web("#0c0c1c"));
        skyLayer.getChildren().add(sky);

        bgImage = new Image(getClass().getResource("resources/background layers.png").toExternalForm());
        bgRect = new Rectangle(WIDTH, HEIGHT);
        bgRect.setMouseTransparent(true);
        bgLayer.getChildren().add(bgRect);

        titleScreen = new TitleScreen(WIDTH, HEIGHT);
        pauseMenu = new PauseMenu(WIDTH, HEIGHT);
        gameOverScreen = new GameOverScreen(WIDTH, HEIGHT);
        victoryScreen = new VictoryScreen(WIDTH, HEIGHT);
        cutscene = new CutsceneSystem(WIDTH, HEIGHT);

        uiLayer.getChildren().addAll(hud.getPane(), titleScreen.getPane(), pauseMenu.getPane(),
            gameOverScreen.getPane(), victoryScreen.getPane(), cutscene.getPane());
        hud.getPane().setVisible(false);

        if (DEBUG) {
            debugText = new javafx.scene.text.Text(12, HEIGHT - 16, "");
            debugText.setFill(Color.LIME);
            debugText.setFont(javafx.scene.text.Font.font("Monospaced", 12));
            uiLayer.getChildren().add(debugText);
        }

        Pane root = new Pane(skyLayer, bgLayer, worldLayer, uiLayer);
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.web("#0c0c1c"));

        scene.setOnKeyPressed(e -> {
            releaseAt.remove(e.getCode());   // a repeat-press cancels the pending release
            onKeyPressed(e.getCode());
        });
        scene.setOnKeyReleased(e -> {
            // Linux fires release+press pairs for a held key; don't drop it
            // immediately - tick() clears it once the grace window passes.
            releaseAt.put(e.getCode(), System.nanoTime());
        });
        scene.setOnMousePressed(e -> {
            if (state == GameState.PLAYING) player.attack();
        });

        stage.setScene(scene);
        stage.setTitle("You Are The Monster - Ludum Dare 33");
        stage.setResizable(false);
        stage.show();
        root.requestFocus();

        showMenu();

        new AnimationTimer() {
            private long last = -1;
            private long acc = 0;

            @Override
            public void handle(long now) {
                if (last < 0) last = now;
                acc += now - last;
                last = now;
                if (acc > TICK_NS * 5) acc = TICK_NS * 5; // avoid spiral of death
                while (acc >= TICK_NS) {
                    tick();
                    acc -= TICK_NS;
                }
                render();
            }
        }.start();
    }

    // ---------------------------------------------------------------- input

    private void onKeyPressed(KeyCode code) {
        switch (state) {
            case MENU:
                if (code == KeyCode.ENTER || code == KeyCode.SPACE) startNewGame();
                else if (code == KeyCode.UP) titleScreen.updateDifficulty(-1);
                else if (code == KeyCode.DOWN) titleScreen.updateDifficulty(1);
                break;
            case PLAYING:
                boolean fresh = keysDown.add(code); // false when this is an OS auto-repeat
                if (code == KeyCode.ESCAPE) {
                    pause();
                } else if (fresh) {
                    if (code == KeyCode.UP || code == KeyCode.SPACE || code == KeyCode.W) {
                        player.jump();
                    } else if (code == KeyCode.Z || code == KeyCode.X || code == KeyCode.CONTROL
                            || code == KeyCode.F || code == KeyCode.SHIFT) {
                        player.attack();
                    } else if (DEBUG && code == KeyCode.G) {
                        Enemy e = new EnemyWerewolf(this, player.getRectangle().x + 70, player.getRectangle().y, 60);
                        addUnit(e);
                        enemiesRemaining++;
                    } else if (DEBUG && code == KeyCode.N) {
                        if (exitBounds != null) levelComplete(false); // skip to next level
                    }
                }
                break;
            case PAUSED:
                if (code == KeyCode.ESCAPE || code == KeyCode.ENTER) resume();
                break;
            case CUTSCENE:
                if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                    if (cutscene.advance()) endCutscene();
                }
                break;
            case GAME_OVER:
                if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                    gameOverScreen.hide();
                    levelManager.reloadCurrentLevel();
                    beginPlaying();
                } else if (code == KeyCode.ESCAPE) {
                    gameOverScreen.hide();
                    showMenu();
                }
                break;
            case LEVEL_COMPLETE:
                if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                    victoryScreen.hide();
                    afterLevelComplete();
                }
                break;
            default:
                break;
        }
    }

    // ---------------------------------------------------------------- loop

    private void tick() {
        // expire keys whose release has outlived the auto-repeat grace window
        long now = System.nanoTime();
        releaseAt.entrySet().removeIf(en -> {
            if (now - en.getValue() >= REPEAT_GRACE_NS) {
                keysDown.remove(en.getKey());
                return true;
            }
            return false;
        });

        if (state != GameState.PLAYING) return;

        int dir = 0;
        if (keysDown.contains(KeyCode.RIGHT) || keysDown.contains(KeyCode.D)) dir += 1;
        if (keysDown.contains(KeyCode.LEFT) || keysDown.contains(KeyCode.A)) dir -= 1;
        player.walk(dir);

        player.update();

        for (Enemy e : new ArrayList<>(enemies)) {
            e.update();
        }

        for (Pickup p : new ArrayList<>(pickups)) {
            p.update();
            if (p.getRectangle().intersects(player.getRectangle())) {
                score += p.apply(player);
                audioManager.play("pickup");
                removePickup(p);
            }
        }

        floatingText.removeIf(dn -> {
            boolean alive = dn.update();
            if (!alive) worldLayer.getChildren().remove(dn.getText());
            return !alive;
        });

        if (player.getHealth() <= 0) {
            gameOver();
            return;
        }

        if (!bossLevel && exitBounds != null && player.getRectangle().intersects(exitBounds)) {
            levelComplete(false);
        }
    }

    private void render() {
        if (player != null) {
            camera.update(player.getRectangle());
        }
        worldLayer.setTranslateX(-camera.getX());
        worldLayer.setTranslateY(-camera.getY());
        bgLayer.setTranslateX(-camera.getX() * 0.4);
        bgLayer.setTranslateY(-camera.getY() * 0.4);

        if (state == GameState.PLAYING && player != null) {
            hud.update(player.getHealth(), player.getMaxHealth(),
                levelManager.getCurrentLevelData().getName(),
                levelManager.getCurrentLevelIndex(), levelManager.getTotalLevels(), score);
        }
        if (exitNode != null) {
            exitNode.setOpacity(0.45 + 0.25 * Math.sin(System.nanoTime() / 2.0e8));
        }
        if (debugText != null && player != null) {
            debugText.setText(String.format("state=%s  player=(%d,%d) v=(%d,%d) ground=%s  cam=(%d,%d)  enemies=%d  keys=%s",
                state, player.getRectangle().x, player.getRectangle().y,
                player.getVelX(), player.getVelY(), player.isOnGround(),
                camera.getX(), camera.getY(), enemies.size(), keysDown));
        }
    }

    // ---------------------------------------------------------------- flow

    private void showMenu() {
        state = GameState.MENU;
        clearWorld();
        hud.getPane().setVisible(false);
        titleScreen.setVisible(true);
        pauseMenu.getPane().setVisible(false);
        gameOverScreen.getPane().setVisible(false);
        victoryScreen.getPane().setVisible(false);
        cutscene.getPane().setVisible(false);
    }

    private void startNewGame() {
        int diff = titleScreen.getSelectedDifficulty();
        difficultyManager.setDifficulty(DifficultyManager.Difficulty.values()[diff]);
        score = 0;
        titleScreen.setVisible(false);
        int startLevel = Integer.getInteger("game.startLevel", 0);
        if (DEBUG && startLevel > 0) {
            levelManager.loadLevel(startLevel);
            beginPlaying();
            return;
        }
        // opening cutscene, then load level 0
        pendingCutsceneAdvanceTo = 0;
        playCutscene(0);
    }

    private void beginPlaying() {
        state = GameState.PLAYING;
        hud.getPane().setVisible(true);
        titleScreen.setVisible(false);
        pauseMenu.getPane().setVisible(false);
        gameOverScreen.getPane().setVisible(false);
        victoryScreen.getPane().setVisible(false);
        cutscene.getPane().setVisible(false);
        keysDown.clear();
        releaseAt.clear();
    }

    private void pause() {
        state = GameState.PAUSED;
        pauseMenu.show();
    }

    private void resume() {
        pauseMenu.hide();
        keysDown.clear();
        releaseAt.clear();
        state = GameState.PLAYING;
    }

    private void gameOver() {
        if (DEBUG) System.err.println("GAME OVER");
        state = GameState.GAME_OVER;
        audioManager.play("game_over");
        gameOverScreen.show();
    }

    private void levelComplete(boolean gameFinished) {
        if (DEBUG) System.err.println("LEVEL COMPLETE finished=" + gameFinished + " idx=" + levelManager.getCurrentLevelIndex());
        state = GameState.LEVEL_COMPLETE;
        audioManager.play("victory");
        if (gameFinished) {
            victoryScreen.show("You broke the curse. The monster endures.", score);
        } else {
            victoryScreen.show(levelManager.getCurrentLevelData().getName() + " cleared", score);
        }
    }

    private void afterLevelComplete() {
        if (bossDefeated) {
            showMenu();
            return;
        }
        if (!levelManager.hasNextLevel()) {
            showMenu();
            return;
        }
        int next = levelManager.getCurrentLevelIndex() + 1;
        boolean newArea = next % 3 == 0;
        if (newArea) {
            int area = next / 3 + 1;          // 2..6
            pendingCutsceneAdvanceTo = next;
            playCutscene(area - 1);          // cutscene index 1..5
        } else {
            levelManager.loadLevel(next);
            beginPlaying();
        }
    }

    private void playCutscene(int index) {
        state = GameState.CUTSCENE;
        hud.getPane().setVisible(false);
        cutscene.startCutscene(index);
    }

    private void endCutscene() {
        cutscene.getPane().setVisible(false);
        if (bossDefeated) {
            levelComplete(true);
            return;
        }
        if (pendingCutsceneAdvanceTo >= 0) {
            int idx = pendingCutsceneAdvanceTo;
            pendingCutsceneAdvanceTo = -1;
            levelManager.loadLevel(idx);
            beginPlaying();
        } else {
            beginPlaying();
        }
    }

    // ---------------------------------------------------------------- world building

    private void clearWorld() {
        worldLayer.getChildren().clear();
        units.clear();
        characters.clear();
        enemies.clear();
        pickups.clear();
        floatingText.clear();
        exitBounds = null;
        exitNode = null;
    }

    /** Called by LevelManager once a {@link LevelData} is parsed. */
    public void buildWorld(LevelData data) {
        clearWorld();
        bossLevel = false;
        bossDefeated = false;
        enemiesRemaining = 0;

        camera.setLevelSize(data.getLevelWidth(), data.getLevelHeight());
        // Parallax backdrop: one image-height tall (so the baked-in horizon never
        // seams vertically), repeated horizontally across the whole level.
        double tileW = bgImage.getWidth() * 2.0;
        int levelH = Math.max(HEIGHT, data.getLevelHeight());
        int bw = Math.max(WIDTH, data.getLevelWidth()) + (int) tileW;
        bgRect.setWidth(bw);
        bgRect.setHeight(levelH);
        bgRect.setFill(new ImagePattern(bgImage, 0, 0, tileW, levelH, false));
        applyAreaTint(data.getArea());

        if (player == null) {
            player = new Player(this, data.getPlayerStartX(), data.getPlayerStartY());
        }
        player.setMaxHealth((int) (100 * difficultyManager.getHealthMultiplier()));
        player.getRectangle().setLocation(data.getPlayerStartX(), data.getPlayerStartY());
        player.resetForLevel();
        addUnit(player);

        for (LevelData.WallData w : data.getWalls()) {
            addUnit(new Wall(w.x, w.y, w.width, w.height));
        }
        for (LevelData.EnemyData ed : data.getEnemies()) {
            Enemy e = createEnemy(ed);
            if (e == null) continue;
            e.scaleStats(difficultyManager.getEnemyHealthMultiplier(), difficultyManager.getDamageMultiplier());
            addUnit(e);
            if (e instanceof BossVampireLord) bossLevel = true;
            else enemiesRemaining++;
        }
        for (LevelData.ItemData it : data.getItems()) {
            spawnPickup(it.type, it.x, it.y);
        }

        // exit portal - a tall doorway so it is reachable from the ground or a jump
        int ex = data.getExitX();
        int ey = data.getExitY();
        exitBounds = new java.awt.Rectangle(ex - 12, ey - 20, 60, 200);
        Rectangle glow = new Rectangle(44, 96);
        glow.setArcWidth(24);
        glow.setArcHeight(24);
        glow.setFill(Color.web("#8a5cff"));
        glow.setStroke(Color.web("#d9c8ff"));
        glow.setStrokeWidth(3);
        glow.setMouseTransparent(true);
        glow.relocate(ex, ey);
        exitNode = glow;
        if (!bossLevel) {
            worldLayer.getChildren().add(glow);
        }
    }

    private void applyAreaTint(int area) {
        ColorAdjust c = new ColorAdjust();
        switch (area) {
            case 2: c.setBrightness(0.04); c.setHue(0.03); break;               // village
            case 3: c.setSaturation(-0.5); c.setBrightness(-0.08); break;        // graveyard
            case 4: c.setBrightness(-0.32); c.setHue(0.12); break;              // caves
            case 5: c.setHue(-0.14); c.setSaturation(-0.2); break;             // castle
            case 6: c.setHue(0.42); c.setSaturation(0.15); c.setBrightness(-0.12); break; // throne
            default: break;                                                     // forest
        }
        bgRect.setEffect(c);
    }

    private Enemy createEnemy(LevelData.EnemyData d) {
        switch (d.type.toLowerCase()) {
            case "bat":         return new EnemyBat(this, d.x, d.y, 22, 28, d.patrolDistance);
            case "werewolf":    return new EnemyWerewolf(this, d.x, d.y, d.patrolDistance);
            case "skeleton":    return new EnemySkeleton(this, d.x, d.y, d.patrolDistance);
            case "guard":       return new EnemyGuard(this, d.x, d.y, d.patrolDistance);
            case "zombie":      return new EnemyZombie(this, d.x, d.y, d.patrolDistance);
            case "spider":      return new EnemySpider(this, d.x, d.y, d.patrolDistance);
            case "knight":      return new EnemyKnight(this, d.x, d.y, d.patrolDistance);
            case "vampirelord": return new BossVampireLord(this, d.x, d.y);
            default:
                System.err.println("Unknown enemy type: " + d.type);
                return null;
        }
    }

    public void spawnPickup(String type, int x, int y) {
        Pickup p = new Pickup(type, x, y);
        pickups.add(p);
        units.add(p);
        Node n = p.getNode();
        if (n != null) {
            worldLayer.getChildren().add(n);
            n.relocate(x, y);
        }
    }

    private void removePickup(Pickup p) {
        pickups.remove(p);
        units.remove(p);
        if (p.getNode() != null) worldLayer.getChildren().remove(p.getNode());
    }

    // ---------------------------------------------------------------- callbacks from entities

    public void onPlayerHitEnemy(Enemy e, int damage) {
        if (DEBUG) System.err.println("HIT " + e.getClass().getSimpleName() + " -" + damage + " hp=" + e.getEnemyHealth());
        audioManager.play("hit");
        DamageNumber dn = new DamageNumber(
            e.getRectangle().getX() + e.getRectangle().getWidth() / 2.0,
            e.getRectangle().getY(), damage, Color.web("#ff5a4a"));
        floatingText.add(dn);
        worldLayer.getChildren().add(dn.getText());
    }

    public void onEnemyKilled(Enemy e) {
        if (DEBUG) System.err.println("KILLED " + e.getClass().getSimpleName() + "  remaining=" + (enemiesRemaining - 1));
        audioManager.play("enemy_down");
        score += 100;
        if (!(e instanceof BossVampireLord)) {
            enemiesRemaining = Math.max(0, enemiesRemaining - 1);
        }
    }

    public void onBossDefeated() {
        if (DEBUG) System.err.println("BOSS DEFEATED -> ending");
        bossDefeated = true;
        // ending cutscene -> final victory screen
        pendingCutsceneAdvanceTo = -1;
        playCutscene(6);
    }

    // ---------------------------------------------------------------- unit registry

    public void addUnit(CollisionUnit unit) {
        units.add(unit);
        if (unit instanceof Character) characters.add((Character) unit);
        if (unit instanceof Enemy) enemies.add((Enemy) unit);
        Node n = unit.getNode();
        if (n != null) {
            worldLayer.getChildren().add(n);
            n.relocate(unit.getRectangle().getX(), unit.getRectangle().getY());
        }
    }

    public void removeUnit(CollisionUnit unit) {
        units.remove(unit);
        characters.remove(unit);
        if (unit instanceof Enemy) enemies.remove(unit);
        Node n = unit.getNode();
        if (n != null) worldLayer.getChildren().remove(n);
    }

    // ---------------------------------------------------------------- accessors

    public Set<CollisionUnit> getCollisionSet() { return units; }
    public Set<Character> getCharacterSet() { return characters; }
    public Camera getCamera() { return camera; }
    public Player getPlayer() { return player; }
    public GameState getCurrentState() { return state; }
    public AudioManager audio() { return audioManager; }
}

import javafx.application.Application;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.Set;
import java.util.HashSet;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;

public class Game extends Application {

    public static final int START_X = 100;
    public static final int START_Y = 500;
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 704;

    private Pane levelPane;
    private Pane uiPane;
    private Player player;
    private Set<CollisionUnit> units;
    private Set<Character> characters;
    private GameState currentState;
    private GameState previousState;
    private Camera camera;
    private HUD hud;
    private LevelManager levelManager;
    private DifficultyManager difficultyManager;
    private AudioManager audioManager;
    private Text debugText;
    private Text levelText;
    private Text difficultyText;
    private int health = 100;
    private int selectedDifficulty = 1; // 0=Easy, 1=Normal, 2=Hard, 3=Nightmare

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        levelPane = new Pane();
        uiPane = new Pane();
        
        // Initialize camera
        camera = new Camera(0, 0, WIDTH, HEIGHT);
        
        // Initialize level manager
        levelManager = new LevelManager(this);
        
        // Initialize difficulty manager
        difficultyManager = new DifficultyManager();
        
        // Initialize audio manager
        audioManager = new AudioManager();
        
        // Initialize HUD
        hud = new HUD();
        uiPane.getChildren().add(hud.getPane());
        
        // Initialize debug text
        debugText = new Text(10, 20, "State: MENU");
        debugText.setFill(Color.WHITE);
        debugText.setFont(Font.font("Monospace", 14));
        uiPane.getChildren().add(debugText);
        
        // Initialize level text
        levelText = new Text(10, 40, "Level: 1/18");
        levelText.setFill(Color.LIGHTGRAY);
        levelText.setFont(Font.font("Monospace", 12));
        uiPane.getChildren().add(levelText);
        
        // Combine panes
        Pane root = new Pane();
        root.getChildren().addAll(levelPane, uiPane);
        
        // Initialize game state
        currentState = GameState.MENU;
        previousState = GameState.MENU;
        
        // Show menu
        showMenu();
        
        initUnits();

        // Key pressed handler
        root.setOnKeyPressed(event -> {
            if (currentState == GameState.PLAYING) {
                if (event.getCode() == KeyCode.RIGHT) {
                    player.moveHorizontal(false, false);
                } else if (event.getCode() == KeyCode.LEFT) {
                    player.moveHorizontal(true, false);
                } else if (event.getCode() == KeyCode.UP) {
                    player.jump();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    pauseGame();
                }
            } else if (currentState == GameState.MENU) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    startGame();
                } else if (event.getCode() == KeyCode.UP) {
                    updateDifficulty(-1);
                } else if (event.getCode() == KeyCode.DOWN) {
                    updateDifficulty(1);
                }
            } else if (currentState == GameState.PAUSED) {
                if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.ENTER) {
                    resumeGame();
                }
            } else if (currentState == GameState.GAME_OVER || currentState == GameState.LEVEL_COMPLETE) {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                    showMenu();
                }
            }
        });

        // Key released handler
        root.setOnKeyReleased(event -> {
            if (currentState == GameState.PLAYING) {
                if (event.getCode() == KeyCode.RIGHT) {
                    player.moveHorizontal(false, true);
                } else if (event.getCode() == KeyCode.LEFT) {
                    player.moveHorizontal(true, true);
                }
            }
        });

        // Mouse click handler for attack
        root.setOnMousePressed(event -> {
            if (currentState == GameState.PLAYING) {
                player.attack();
            }
        });

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("You Are The Monster - Ludum Dare 33");
        primaryStage.show();

        // Game loop
        try {
            Task updateTask = new Task<Void>() {
                public Void call() throws Exception {
                    while (true) {
                        Platform.runLater(() -> {
                            updateAll();
                        });
                        Thread.sleep(50);
                    }
                }
            };
            Thread thread = new Thread(updateTask);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            System.out.println("Error starting game loop: " + e.getMessage());
        }

        root.requestFocus();
    }

    private void showMenu() {
        currentState = GameState.MENU;
        levelPane.getChildren().clear();
        
        // Show menu text
        Text titleText = new Text(WIDTH/2 - 280, HEIGHT/2 - 80, "YOU ARE THE MONSTER");
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Monospace", 48));
        levelPane.getChildren().add(titleText);
        
        Text startText = new Text(WIDTH/2 - 150, HEIGHT/2 - 20, "Press ENTER or SPACE to start");
        startText.setFill(Color.LIGHTGRAY);
        startText.setFont(Font.font("Monospace", 20));
        levelPane.getChildren().add(startText);
        
        // Difficulty selection
        Text diffLabel = new Text(WIDTH/2 - 150, HEIGHT/2 + 30, "Difficulty (UP/DOWN to change):");
        diffLabel.setFill(Color.LIGHTGRAY);
        diffLabel.setFont(Font.font("Monospace", 16));
        levelPane.getChildren().add(diffLabel);
        
        difficultyText = new Text(WIDTH/2 - 100, HEIGHT/2 + 60, getDifficultyName());
        difficultyText.setFill(getDifficultyColor());
        difficultyText.setFont(Font.font("Monospace", 24));
        levelPane.getChildren().add(difficultyText);
        
        Text controlsText = new Text(WIDTH/2 - 200, HEIGHT/2 + 100, "Controls: Arrow Keys to move, UP to jump, Click to attack, ESC to pause");
        controlsText.setFill(Color.GRAY);
        controlsText.setFont(Font.font("Monospace", 14));
        levelPane.getChildren().add(controlsText);
    }

    private String getDifficultyName() {
        switch (selectedDifficulty) {
            case 0: return "Easy";
            case 1: return "Normal";
            case 2: return "Hard";
            case 3: return "Nightmare";
            default: return "Normal";
        }
    }

    private Color getDifficultyColor() {
        switch (selectedDifficulty) {
            case 0: return Color.GREEN;
            case 1: return Color.WHITE;
            case 2: return Color.ORANGE;
            case 3: return Color.RED;
            default: return Color.WHITE;
        }
    }

    private void updateDifficulty(int direction) {
        selectedDifficulty += direction;
        if (selectedDifficulty < 0) selectedDifficulty = 3;
        if (selectedDifficulty > 3) selectedDifficulty = 0;
        
        // Update difficulty manager
        DifficultyManager.Difficulty[] difficulties = {
            DifficultyManager.Difficulty.EASY,
            DifficultyManager.Difficulty.NORMAL,
            DifficultyManager.Difficulty.HARD,
            DifficultyManager.Difficulty.NIGHTMARE
        };
        difficultyManager.setDifficulty(difficulties[selectedDifficulty]);
        
        // Update UI
        if (difficultyText != null) {
            difficultyText.setText(getDifficultyName());
            difficultyText.setFill(getDifficultyColor());
        }
    }

    private void startGame() {
        currentState = GameState.PLAYING;
        levelPane.getChildren().clear();
        initUnits();
        levelPane.getChildren().clear();
        units.clear();
        characters.clear();
        levelManager.loadLevel(0);
    }

    private void pauseGame() {
        previousState = currentState;
        currentState = GameState.PAUSED;
        
        // Show pause overlay
        Text pauseText = new Text(WIDTH/2 - 100, HEIGHT/2, "PAUSED");
        pauseText.setFill(Color.WHITE);
        pauseText.setFont(Font.font("Monospace", 36));
        pauseText.setId("pauseText");
        levelPane.getChildren().add(pauseText);
        
        Text resumeText = new Text(WIDTH/2 - 150, HEIGHT/2 + 50, "Press ENTER or ESCAPE to resume");
        resumeText.setFill(Color.LIGHTGRAY);
        resumeText.setFont(Font.font("Monospace", 16));
        resumeText.setId("resumeText");
        levelPane.getChildren().add(resumeText);
    }

    private void resumeGame() {
        currentState = previousState;
        // Remove pause overlay
        levelPane.getChildren().removeIf(node -> 
            node.getId() != null && 
            (node.getId().equals("pauseText") || node.getId().equals("resumeText"))
        );
    }

    private void gameOver() {
        currentState = GameState.GAME_OVER;
        levelPane.getChildren().clear();
        
        Text gameOverText = new Text(WIDTH/2 - 150, HEIGHT/2 - 50, "GAME OVER");
        gameOverText.setFill(Color.RED);
        gameOverText.setFont(Font.font("Monospace", 48));
        levelPane.getChildren().add(gameOverText);
        
        Text retryText = new Text(WIDTH/2 - 150, HEIGHT/2 + 50, "Press ENTER or SPACE to retry");
        retryText.setFill(Color.LIGHTGRAY);
        retryText.setFont(Font.font("Monospace", 16));
        levelPane.getChildren().add(retryText);
    }

    private void levelComplete() {
        currentState = GameState.LEVEL_COMPLETE;
        levelPane.getChildren().clear();
        
        Text completeText = new Text(WIDTH/2 - 200, HEIGHT/2 - 50, "LEVEL COMPLETE!");
        completeText.setFill(Color.GREEN);
        completeText.setFont(Font.font("Monospace", 36));
        levelPane.getChildren().add(completeText);
        
        Text nextText = new Text(WIDTH/2 - 150, HEIGHT/2 + 50, "Press ENTER or SPACE to continue");
        nextText.setFill(Color.LIGHTGRAY);
        nextText.setFont(Font.font("Monospace", 16));
        levelPane.getChildren().add(nextText);
    }

    private void initUnits() {
        units = new HashSet<>();
        characters = new HashSet<>();
        player = new Player(this, START_X, START_Y);
        addUnit(player);
        initWalls();
        initEnemies();
    }

    private void initWalls() {
        Wall ground = new Wall(0, HEIGHT- 60, WIDTH, 40);
        addUnit(ground);
        addUnit(new Wall(0, 0, 160, 288));
        addUnit(new Wall(0, 288, 96, 64));
        addUnit(new Wall(0, 352, 32, 320));
        addUnit(new Wall(1120, 0, 160, 288));
        addUnit(new Wall(1184, 288, 96, 64));
        addUnit(new Wall(1248, 352, 32, 320));
        addUnit(new Wall(192, 480, 256, 32));
        addUnit(new Wall(480, 608, 96, 32));
        addUnit(new Wall(608, 576, 96, 32));
        addUnit(new Wall(704, 512, 192, 32));
    }

    private void initEnemies() {
        // Add some bats
        EnemyBat bat1 = new EnemyBat(this, 400, 400, 20, 20, 200);
        addUnit(bat1);
        
        EnemyBat bat2 = new EnemyBat(this, 800, 300, 20, 20, 150);
        addUnit(bat2);
        
        // Add a werewolf
        EnemyWerewolf wolf = new EnemyWerewolf(this, 600, 500, 300);
        addUnit(wolf);
    }

    public Set<CollisionUnit> getCollisionSet() {
        return units;
    }

    public Set<Character> getCharacterSet() {
        return characters;
    }

    private void updateAll() {
        if (currentState != GameState.PLAYING) {
            return;
        }
        
        // Update all characters
        for (Character item : characters) {
            item.update();
        }
        
        // Check player health
        if (player.getHealth() <= 0) {
            gameOver();
        }
        
        // Update camera to follow player
        camera.update(player.getRectangle());
        
        // Update HUD
        hud.update(player.getHealth(), player.getMaxHealth());
        
        // Update debug text
        debugText.setText("State: " + currentState + " | Health: " + player.getHealth());
        
        // Update level text
        levelText.setText("Level: " + (levelManager.getCurrentLevelIndex() + 1) + "/" + levelManager.getTotalLevels());
    }

    public void addUnit(CollisionUnit unit) {
        ImageView image = unit.getImageView();
        if (image != null) {
            levelPane.getChildren().add(image);
            image.setLayoutX((int)unit.getRectangle().getX());
            image.setLayoutY((int)unit.getRectangle().getY());
        }
        units.add(unit);
        if (unit instanceof Character) {
            characters.add((Character) unit);
        }
    }

    public void removeUnit(CollisionUnit unit) {
        ImageView image = unit.getImageView();
        if (image != null) {
            levelPane.getChildren().remove(image);
        }
        units.remove(unit);
        if (unit instanceof Character) {
            characters.remove((Character) unit);
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(GameState state) {
        this.currentState = state;
    }

    public Camera getCamera() {
        return camera;
    }

    public Player getPlayer() {
        return player;
    }
}

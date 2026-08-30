import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;

public class Player extends Character {

    public static final int PLAYER_WIDTH = 30;
    public static final int PLAYER_HEIGHT = 44;
    public static final int ATTACK_SPACE = 6;

    private int maxHealth = 100;
    private static final int ATTACK_COOLDOWN_FRAMES = 10;

    private boolean secondJumpAvailable;
    private final List<Attack> activeAttacks = new ArrayList<>();
    private int attackCooldown;

    public Player(Game game, int x, int y) {
        super(game, x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        movementFactor = 6;
        jumpFactor = -15;
        health = maxHealth;
        loadAnim("resources/player", 6, 3, "FALL");
    }

    @Override
    public Node getNode() {
        return animation.getImageView();
    }

    public int getHealth() { return health; }

    public int getMaxHealth() { return maxHealth; }

    public void setMaxHealth(int max) {
        this.maxHealth = max;
        this.health = max;
    }

    public void resetForLevel() {
        health = maxHealth;
        velocity.horizontal = 0;
        velocity.vertical = 0;
        inputDir = 0;
        secondJumpAvailable = false;
        invincible = false;
        invincibilityTimer = 0;
        for (Attack a : activeAttacks) game.removeUnit(a);
        activeAttacks.clear();
    }

    public void heal(int amount) {
        health = Math.min(health + amount, maxHealth);
    }

    @Override
    public void jump() {
        if (isOnGround()) {
            velocity.vertical = jumpFactor;
            secondJumpAvailable = true;
            game.audio().play("jump");
        } else if (secondJumpAvailable) {
            velocity.vertical = jumpFactor;
            secondJumpAvailable = false;
            game.audio().play("jump");
        }
    }

    public void attack() {
        if (attackCooldown > 0) return;
        attackCooldown = ATTACK_COOLDOWN_FRAMES;
        game.audio().play("attack");

        int ax = left
            ? (int) rectangle.getX() - Attack.ATTACK_WIDTH + ATTACK_SPACE
            : (int) rectangle.getX() + (int) rectangle.getWidth() - ATTACK_SPACE;
        int ay = (int) rectangle.getY() + (int) rectangle.getHeight() / 2 - Attack.ATTACK_HEIGHT / 2;

        Attack attack = new Attack(game, ax, ay, left);
        game.addUnit(attack);
        activeAttacks.add(attack);
    }

    @Override
    public void update() {
        super.update();

        if (isOnGround() && velocity.vertical >= 0) {
            secondJumpAvailable = false;
        }
        if (attackCooldown > 0) attackCooldown--;

        activeAttacks.removeIf(attack -> {
            attack.getRectangle().setLocation(
                left ? (int) rectangle.getX() - Attack.ATTACK_WIDTH + ATTACK_SPACE
                     : (int) rectangle.getX() + (int) rectangle.getWidth() - ATTACK_SPACE,
                (int) rectangle.getY() + (int) rectangle.getHeight() / 2 - Attack.ATTACK_HEIGHT / 2);
            boolean alive = attack.update();
            if (!alive) game.removeUnit(attack);
            return !alive;
        });

    }

    @Override
    public void damageHealth(int damage) {
        if (invincible) return;
        int before = health;
        super.damageHealth(damage);
        if (health < before) game.audio().play("hurt");
    }
}

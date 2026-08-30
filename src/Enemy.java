import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.effect.ColorAdjust;

public abstract class Enemy extends Character {

    public enum EnemyState {
        IDLE,
        PATROL,
        CHASE,
        ATTACK,
        HURT,
        DEAD
    }

    protected EnemyState enemyState;
    protected int enemyHealth;
    protected int maxEnemyHealth;
    protected int attackDamage;
    protected int attackRange;
    protected int attackCooldown;
    protected int currentAttackCooldown;
    protected boolean facingRight;
    protected Rectangle visionBox;
    protected int visionRange;
    protected Player targetPlayer;
    protected int hurtTimer;
    protected static final int HURT_DURATION = 8;
    protected int deathTimer;
    protected static final int DEATH_DURATION = 18;
    protected boolean dropsLoot;

    public Enemy(Game game, int x, int y, int width, int height, int health, int damage) {
        super(game, x, y, width, height);
        this.enemyHealth = health;
        this.maxEnemyHealth = health;
        this.attackDamage = damage;
        this.attackRange = 40;
        this.attackCooldown = 45;
        this.currentAttackCooldown = 0;
        this.facingRight = true;
        this.visionRange = 200;
        this.visionBox = new Rectangle(x - visionRange, y - visionRange / 2,
            width + visionRange * 2, height + visionRange);
        this.enemyState = EnemyState.IDLE;
        this.hurtTimer = 0;
        this.deathTimer = 0;
        this.dropsLoot = true;
    }

    /**
     * Colour-shift the shared sprite so re-used art (player / bat frames) reads as
     * a distinct creature. Stopgap until bespoke sprites exist for each enemy type.
     */
    protected void tint(double hue, double saturation, double brightness) {
        ColorAdjust c = new ColorAdjust();
        c.setHue(hue);
        c.setSaturation(saturation);
        c.setBrightness(brightness);
        animation.getImageView().setEffect(c);
    }

    @Override
    public void update() {
        if (enemyState == EnemyState.DEAD) {
            deathTimer++;
            animation.getImageView().setOpacity(Math.max(0, 1.0 - (double) deathTimer / DEATH_DURATION));
            animation.getImageView().setRotate(deathTimer * 5);
            if (deathTimer >= DEATH_DURATION) {
                game.removeUnit(this);
            }
            return;
        }

        targetPlayer = game.getPlayer();
        inputDir = 0;
        inputVert = 0;

        switch (enemyState) {
            case IDLE:   updateIdle();   break;
            case PATROL: updatePatrol(); break;
            case CHASE:  updateChase();  break;
            case ATTACK: updateAttack(); break;
            case HURT:   updateHurt();   break;
            default: break;
        }

        visionBox.setLocation((int) rectangle.getX() - visionRange,
            (int) rectangle.getY() - visionRange / 2);

        if (inputDir > 0) facingRight = true;
        else if (inputDir < 0) facingRight = false;

        if (currentAttackCooldown > 0) currentAttackCooldown--;
        if (hurtTimer > 0) hurtTimer--;

        super.update();
    }

    protected abstract void updateIdle();
    protected abstract void updatePatrol();
    protected abstract void updateChase();
    protected abstract void updateAttack();

    protected void updateHurt() {
        inputDir = 0;
        if (hurtTimer <= 0) {
            enemyState = EnemyState.PATROL;
        }
    }

    public void takeDamage(int damage) {
        if (enemyState == EnemyState.DEAD) return;

        enemyHealth -= damage;
        hurtTimer = HURT_DURATION;
        enemyState = EnemyState.HURT;

        if (targetPlayer != null) {
            int away = (targetPlayer.getRectangle().getX() < rectangle.getX()) ? 10 : -10;
            applyKnockback(away, flying ? 0 : -8);
        }

        if (enemyHealth <= 0) {
            die();
        }
    }

    protected void die() {
        enemyState = EnemyState.DEAD;
        deathTimer = 0;
        inputDir = 0;
        velocity.horizontal = 0;
        velocity.vertical = -5;
        game.onEnemyKilled(this);
        if (dropsLoot && Math.random() < 0.35) {
            game.spawnPickup("meat", (int) rectangle.getX(), (int) rectangle.getY());
        }
    }

    protected boolean canSeePlayer() {
        if (targetPlayer == null) return false;
        return visionBox.intersects(targetPlayer.getRectangle());
    }

    protected boolean isPlayerInRange(int range) {
        if (targetPlayer == null) return false;
        double distance = rectangle.getLocation().distance(targetPlayer.getRectangle().getLocation());
        return distance <= range;
    }

    protected void moveTowardPlayer() {
        if (targetPlayer == null) return;
        walk(targetPlayer.getRectangle().getX() < rectangle.getX() ? -1 : 1);
    }

    protected void moveAwayFromPlayer() {
        if (targetPlayer == null) return;
        walk(targetPlayer.getRectangle().getX() < rectangle.getX() ? 1 : -1);
    }

    public int getEnemyHealth() { return enemyHealth; }

    public int getMaxEnemyHealth() { return maxEnemyHealth; }

    public void scaleStats(double healthMul, double damageMul) {
        this.maxEnemyHealth = (int) Math.max(1, this.maxEnemyHealth * healthMul);
        this.enemyHealth = this.maxEnemyHealth;
        this.attackDamage = (int) Math.max(1, this.attackDamage * damageMul);
    }

    public EnemyState getEnemyState() { return enemyState; }

    public boolean isDead() { return enemyState == EnemyState.DEAD; }
}

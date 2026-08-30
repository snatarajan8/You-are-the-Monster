import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

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
    protected static final int HURT_DURATION = 10;
    protected int deathTimer;
    protected static final int DEATH_DURATION = 30;
    protected boolean dropsLoot;
    protected List<String> lootTable;

    public Enemy(Game game, int x, int y, int width, int height, int health, int damage) {
        super(game, x, y, width, height);
        this.enemyHealth = health;
        this.maxEnemyHealth = health;
        this.attackDamage = damage;
        this.attackRange = 40;
        this.attackCooldown = 60;
        this.currentAttackCooldown = 0;
        this.facingRight = true;
        this.visionRange = 200;
        this.visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            width + visionRange * 2, height + visionRange);
        this.enemyState = EnemyState.IDLE;
        this.hurtTimer = 0;
        this.deathTimer = 0;
        this.dropsLoot = true;
        this.lootTable = new ArrayList<>();
        this.lootTable.add("meat");
    }

    @Override
    public void update() {
        if (enemyState == EnemyState.DEAD) {
            deathTimer++;
            if (deathTimer >= DEATH_DURATION) {
                game.removeUnit(this);
            }
            return;
        }

        // Find player
        findTargetPlayer();

        // State machine
        switch (enemyState) {
            case IDLE:
                updateIdle();
                break;
            case PATROL:
                updatePatrol();
                break;
            case CHASE:
                updateChase();
                break;
            case ATTACK:
                updateAttack();
                break;
            case HURT:
                updateHurt();
                break;
        }

        // Update vision box position
        visionBox.setLocation((int) rectangle.getX() - visionRange, 
            (int) rectangle.getY() - visionRange/2);

        // Update facing direction
        if (velocity.horizontal > 0) {
            facingRight = true;
        } else if (velocity.horizontal < 0) {
            facingRight = false;
        }

        // Update cooldowns
        if (currentAttackCooldown > 0) {
            currentAttackCooldown--;
        }
        if (hurtTimer > 0) {
            hurtTimer--;
        }

        // Call parent update for physics
        super.update();
    }

    protected void findTargetPlayer() {
        for (CollisionUnit unit : units) {
            if (unit instanceof Player) {
                targetPlayer = (Player) unit;
                break;
            }
        }
    }

    protected abstract void updateIdle();
    protected abstract void updatePatrol();
    protected abstract void updateChase();
    protected abstract void updateAttack();

    protected void updateHurt() {
        velocity.horizontal = 0;
        if (hurtTimer <= 0) {
            enemyState = EnemyState.PATROL;
        }
    }

    public void takeDamage(int damage) {
        if (enemyState == EnemyState.DEAD) return;
        
        enemyHealth -= damage;
        hurtTimer = HURT_DURATION;
        enemyState = EnemyState.HURT;
        
        // Knockback
        if (targetPlayer != null) {
            if (targetPlayer.getRectangle().getX() < rectangle.getX()) {
                velocity.horizontal = 15;
            } else {
                velocity.horizontal = -15;
            }
            velocity.vertical = -10;
        }
        
        if (enemyHealth <= 0) {
            die();
        }
    }

    protected void die() {
        enemyState = EnemyState.DEAD;
        deathTimer = 0;
        velocity.horizontal = 0;
        velocity.vertical = -5; // Pop up effect
        
        // Drop loot
        if (dropsLoot && !lootTable.isEmpty()) {
            dropLoot();
        }
    }

    protected void dropLoot() {
        // Simple loot drop - spawn meat at enemy position
        // This will be expanded later with proper item system
        System.out.println("Enemy dropped loot!");
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
        
        if (targetPlayer.getRectangle().getX() < rectangle.getX()) {
            moveHorizontal(true, true);
        } else {
            moveHorizontal(false, true);
        }
    }

    protected void moveAwayFromPlayer() {
        if (targetPlayer == null) return;
        
        if (targetPlayer.getRectangle().getX() < rectangle.getX()) {
            moveHorizontal(false, true);
        } else {
            moveHorizontal(true, true);
        }
    }

    public int getEnemyHealth() {
        return enemyHealth;
    }

    public int getMaxEnemyHealth() {
        return maxEnemyHealth;
    }

    public EnemyState getEnemyState() {
        return enemyState;
    }

    public boolean isDead() {
        return enemyState == EnemyState.DEAD;
    }
}

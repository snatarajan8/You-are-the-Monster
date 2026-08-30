import java.awt.Rectangle;

public class EnemyBat extends Enemy {

    public static final int BAT_WIDTH = 20;
    public static final int BAT_HEIGHT = 20;
    private static final int BAT_HEALTH = 30;
    private static final int BAT_DAMAGE = 5;
    private static final int PATROL_SPEED = 3;
    private static final int CHASE_SPEED = 5;

    private int distance;
    private int travelled;
    private int patrolDirection;

    public EnemyBat(Game game, int x, int y, int height, int width, int patrolDistance) {
        super(game, x, y, height, width, BAT_HEALTH, BAT_DAMAGE);
        movementFactor = PATROL_SPEED;
        distance = patrolDistance;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 150;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            width + visionRange * 2, height + visionRange);
        enemyState = EnemyState.PATROL;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        animation.addSequence(State.FACELEFT, animArray("resources/Bat/MOVELEFT", 1));
        animation.addSequence(State.FACERIGHT, animArray("resources/Bat/MOVERIGHT", 1));
        animation.addSequence(State.MOVELEFT, animArray("resources/Bat/MOVELEFT", 4));
        animation.addSequence(State.MOVERIGHT, animArray("resources/Bat/MOVERIGHT", 4));
        animation.addSequence(State.FALLLEFT, animArray("resources/Bat/MOVELEFT", 1));
        animation.addSequence(State.FALLRIGHT, animArray("resources/Bat/MOVERIGHT", 1));
    }

    @Override
    protected void updateIdle() {
        // Bats are always patrolling
        enemyState = EnemyState.PATROL;
    }

    @Override
    protected void updatePatrol() {
        movementFactor = PATROL_SPEED;
        
        // Move back and forth
        if (patrolDirection > 0) {
            moveHorizontal(true, false);  // Move right
        } else {
            moveHorizontal(false, false);  // Move left
        }
        
        travelled += Math.abs(velocity.horizontal);
        
        if (travelled >= distance) {
            patrolDirection *= -1;
            travelled = 0;
        }
        
        // Check if player is visible
        if (canSeePlayer()) {
            enemyState = EnemyState.CHASE;
        }
    }

    @Override
    protected void updateChase() {
        movementFactor = CHASE_SPEED;
        
        if (!canSeePlayer()) {
            enemyState = EnemyState.PATROL;
            return;
        }
        
        if (targetPlayer != null) {
            if (targetPlayer.getRectangle().getX() < rectangle.getX()) {
                moveHorizontal(true, false);  // Move left toward player
            } else {
                moveHorizontal(false, false);  // Move right toward player
            }
        }
        
        // Check if in attack range
        if (isPlayerInRange(attackRange)) {
            enemyState = EnemyState.ATTACK;
        }
    }

    @Override
    protected void updateAttack() {
        velocity.horizontal = 0;
        
        if (currentAttackCooldown <= 0) {
            // Deal damage to player
            if (targetPlayer != null && isPlayerInRange(attackRange)) {
                targetPlayer.damageHealth(attackDamage);
                currentAttackCooldown = attackCooldown;
            }
            enemyState = EnemyState.CHASE;
        }
    }
}

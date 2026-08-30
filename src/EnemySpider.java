import java.awt.Rectangle;

public class EnemySpider extends Enemy {

    public static final int SPIDER_WIDTH = 24;
    public static final int SPIDER_HEIGHT = 16;
    private static final int SPIDER_HEALTH = 25;
    private static final int SPIDER_DAMAGE = 6;
    private static final int PATROL_SPEED = 3;
    private static final int CHASE_SPEED = 5;

    private int distance;
    private int travelled;
    private int patrolDirection;

    public EnemySpider(Game game, int x, int y, int patrolArea) {
        super(game, x, y, SPIDER_WIDTH, SPIDER_HEIGHT, SPIDER_HEALTH, SPIDER_DAMAGE);
        distance = patrolArea;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 120;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            SPIDER_WIDTH + visionRange * 2, SPIDER_HEIGHT + visionRange);
        enemyState = EnemyState.PATROL;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        // Spider uses bat sprites as placeholder
        animation.addSequence(State.MOVELEFT, animArray("resources/Bat/MOVELEFT", 4));
        animation.addSequence(State.MOVERIGHT, animArray("resources/Bat/MOVERIGHT", 4));
    }

    @Override
    protected void updateIdle() {
        velocity.horizontal = 0;
        if (canSeePlayer()) {
            enemyState = EnemyState.CHASE;
        }
    }

    @Override
    protected void updatePatrol() {
        movementFactor = PATROL_SPEED;
        
        if (patrolDirection > 0) {
            moveHorizontal(false, true);
        } else {
            moveHorizontal(true, true);
        }
        
        travelled += Math.abs(velocity.horizontal);
        
        if (travelled >= distance) {
            patrolDirection *= -1;
            travelled = 0;
        }
        
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
        
        moveTowardPlayer();
        
        if (isPlayerInRange(attackRange)) {
            enemyState = EnemyState.ATTACK;
        }
    }

    @Override
    protected void updateAttack() {
        velocity.horizontal = 0;
        
        if (currentAttackCooldown <= 0) {
            if (targetPlayer != null && isPlayerInRange(attackRange)) {
                targetPlayer.damageHealth(attackDamage);
                currentAttackCooldown = attackCooldown;
            }
            enemyState = EnemyState.CHASE;
        }
    }
}

import java.awt.Rectangle;

public class EnemyZombie extends Enemy {

    public static final int ZOMBIE_WIDTH = 28;
    public static final int ZOMBIE_HEIGHT = 32;
    private static final int ZOMBIE_HEALTH = 45;
    private static final int ZOMBIE_DAMAGE = 8;
    private static final int PATROL_SPEED = 1;
    private static final int CHASE_SPEED = 2;

    private int distance;
    private int travelled;
    private int patrolDirection;

    public EnemyZombie(Game game, int x, int y, int patrolArea) {
        super(game, x, y, ZOMBIE_WIDTH, ZOMBIE_HEIGHT, ZOMBIE_HEALTH, ZOMBIE_DAMAGE);
        distance = patrolArea;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 150;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            ZOMBIE_WIDTH + visionRange * 2, ZOMBIE_HEIGHT + visionRange);
        enemyState = EnemyState.PATROL;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        // Zombie uses modified player sprites
        animation.addSequence(State.FACELEFT, animArray("resources/player/FACELEFT", 3));
        animation.addSequence(State.FACERIGHT, animArray("resources/player/FACERIGHT", 3));
        animation.addSequence(State.MOVELEFT, animArray("resources/player/MOVELEFT", 6));
        animation.addSequence(State.MOVERIGHT, animArray("resources/player/MOVERIGHT", 6));
        animation.addSequence(State.FALLLEFT, animArray("resources/player/FACELEFT", 1));
        animation.addSequence(State.FALLRIGHT, animArray("resources/player/FACERIGHT", 1));
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

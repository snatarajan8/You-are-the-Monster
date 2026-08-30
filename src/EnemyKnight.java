import java.awt.Rectangle;

public class EnemyKnight extends Enemy {

    public static final int KNIGHT_WIDTH = 32;
    public static final int KNIGHT_HEIGHT = 36;
    private static final int KNIGHT_HEALTH = 80;
    private static final int KNIGHT_DAMAGE = 15;
    private static final int PATROL_SPEED = 2;
    private static final int CHASE_SPEED = 3;

    private int distance;
    private int travelled;
    private int patrolDirection;

    public EnemyKnight(Game game, int x, int y, int patrolArea) {
        super(game, x, y, KNIGHT_WIDTH, KNIGHT_HEIGHT, KNIGHT_HEALTH, KNIGHT_DAMAGE);
        distance = patrolArea;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 220;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            KNIGHT_WIDTH + visionRange * 2, KNIGHT_HEIGHT + visionRange);
        enemyState = EnemyState.PATROL;
        attackRange = 50;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        // Knight uses modified player sprites
        animation.addSequence(State.FACELEFT, animArray("resources/player/FACELEFT", 3));
        animation.addSequence(State.FACERIGHT, animArray("resources/player/FACERIGHT", 3));
        animation.addSequence(State.MOVELEFT, animArray("resources/player/MOVELEFT", 6));
        animation.addSequence(State.MOVERIGHT, animArray("resources/player/MOVERIGHT", 6));
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

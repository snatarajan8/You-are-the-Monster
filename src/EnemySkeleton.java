import java.awt.Rectangle;
import java.util.Random;

public class EnemySkeleton extends Enemy {

    public static final int SKELETON_WIDTH = 24;
    public static final int SKELETON_HEIGHT = 32;
    private static final int SKELETON_HEALTH = 40;
    private static final int SKELETON_DAMAGE = 8;
    private static final int PATROL_SPEED = 2;
    private static final int CHASE_SPEED = 4;

    private int distance;
    private int travelled;
    private int patrolDirection;
    private Random random;
    private int idleTimer;
    private static final int IDLE_DURATION = 60;

    public EnemySkeleton(Game game, int x, int y, int patrolArea) {
        super(game, x, y, SKELETON_WIDTH, SKELETON_HEIGHT, SKELETON_HEALTH, SKELETON_DAMAGE);
        distance = patrolArea;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 180;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            SKELETON_WIDTH + visionRange * 2, SKELETON_HEIGHT + visionRange);
        enemyState = EnemyState.IDLE;
        random = new Random();
        idleTimer = random.nextInt(IDLE_DURATION);
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        animation.addSequence(State.FACELEFT, animArray("resources/skeleton/FACELEFT", 3));
        animation.addSequence(State.FACERIGHT, animArray("resources/skeleton/FACERIGHT", 3));
        animation.addSequence(State.MOVELEFT, animArray("resources/skeleton/MOVELEFT", 6));
        animation.addSequence(State.MOVERIGHT, animArray("resources/skeleton/MOVERIGHT", 6));
        animation.addSequence(State.FALLLEFT, animArray("resources/skeleton/FACELEFT", 1));
        animation.addSequence(State.FALLRIGHT, animArray("resources/skeleton/FACERIGHT", 1));
    }

    @Override
    protected void updateIdle() {
        velocity.horizontal = 0;
        idleTimer--;
        
        if (idleTimer <= 0) {
            enemyState = EnemyState.PATROL;
            patrolDirection = random.nextBoolean() ? 1 : -1;
        }
        
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
            enemyState = EnemyState.IDLE;
            idleTimer = IDLE_DURATION;
            travelled = 0;
            velocity.horizontal = 0;
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

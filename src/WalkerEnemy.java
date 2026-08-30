import java.awt.Rectangle;
import java.util.Random;

/**
 * Shared behaviour for every ground-bound enemy: walk a patrol span, chase the
 * player on sight, wind up and hit in melee range. Subclasses only supply stats,
 * sprites and a colour tint.
 */
public abstract class WalkerEnemy extends Enemy {

    protected final int patrolSpeed;
    protected final int chaseSpeed;
    protected int distance;
    protected int travelled;
    protected int patrolDirection;
    protected int idleTimer;
    protected boolean pausesAtEnds;      // skeletons stop and look around; others turn on the spot
    protected static final int IDLE_DURATION = 45;
    protected int windup;
    protected static final int ATTACK_WINDUP = 8;
    protected final Random random = new Random();

    public WalkerEnemy(Game game, int x, int y, int width, int height,
                       int health, int damage, int patrolSpeed, int chaseSpeed,
                       int patrolDistance, int visionRange) {
        super(game, x, y, width, height, health, damage);
        this.patrolSpeed = patrolSpeed;
        this.chaseSpeed = chaseSpeed;
        this.distance = Math.max(40, patrolDistance);
        this.travelled = 0;
        this.patrolDirection = random.nextBoolean() ? 1 : -1;
        this.visionRange = visionRange;
        this.visionBox = new Rectangle(x - visionRange, y - visionRange / 2,
            width + visionRange * 2, height + visionRange);
        this.enemyState = EnemyState.PATROL;
    }

    @Override
    protected void updateIdle() {
        inputDir = 0;
        idleTimer--;
        if (idleTimer <= 0) {
            enemyState = EnemyState.PATROL;
            patrolDirection = random.nextBoolean() ? 1 : -1;
        }
        if (canSeePlayer()) enemyState = EnemyState.CHASE;
    }

    @Override
    protected void updatePatrol() {
        movementFactor = patrolSpeed;
        // turn back at a ledge or a wall so patrols stay on their platform
        if (isOnGround() && (!groundAhead(patrolDirection) || wallAhead(patrolDirection))) {
            patrolDirection *= -1;
            travelled = 0;
        }
        walk(patrolDirection);
        travelled += patrolSpeed;
        if (travelled >= distance) {
            travelled = 0;
            patrolDirection *= -1;
            if (pausesAtEnds) {
                enemyState = EnemyState.IDLE;
                idleTimer = IDLE_DURATION;
            }
        }
        if (canSeePlayer()) enemyState = EnemyState.CHASE;
    }

    @Override
    protected void updateChase() {
        movementFactor = chaseSpeed;
        if (!canSeePlayer()) {
            enemyState = EnemyState.PATROL;
            return;
        }
        int toPlayer = targetPlayer.getRectangle().getX() < rectangle.getX() ? -1 : 1;
        // don't chase off a cliff - hold position and wait
        if (isOnGround() && !groundAhead(toPlayer)) {
            inputDir = 0;
        } else {
            moveTowardPlayer();
        }
        if (isPlayerInRange(attackRange)) {
            enemyState = EnemyState.ATTACK;
            windup = ATTACK_WINDUP;
        }
    }

    @Override
    protected void updateAttack() {
        inputDir = 0;
        if (windup > 0) {
            windup--;
            return;
        }
        if (currentAttackCooldown <= 0) {
            if (targetPlayer != null && isPlayerInRange(attackRange + 6)) {
                targetPlayer.damageHealth(attackDamage);
            }
            currentAttackCooldown = attackCooldown;
        }
        enemyState = EnemyState.CHASE;
    }
}

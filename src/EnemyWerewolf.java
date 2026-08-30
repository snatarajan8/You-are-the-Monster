import java.awt.Rectangle;

public class EnemyWerewolf extends Enemy {

    public static final int WOLF_WIDTH = 32;
    public static final int WOLF_HEIGHT = 32;
    private static final int WOLF_HEALTH = 60;
    private static final int WOLF_DAMAGE = 10;
    private static final int PATROL_SPEED = 4;
    private static final int CHASE_SPEED = 7;

    private int distance;
    private int travelled;
    private int patrolDirection;

    public EnemyWerewolf(Game game, int x, int y, int patrolArea) {
        super(game, x, y, WOLF_WIDTH, WOLF_HEIGHT, WOLF_HEALTH, WOLF_DAMAGE);
        distance = patrolArea;
        travelled = 0;
        patrolDirection = 1;
        visionRange = 250;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            WOLF_WIDTH + visionRange * 2, WOLF_HEIGHT + visionRange);
        enemyState = EnemyState.PATROL;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        animation.addSequence(State.FACELEFT, animArray("resources/werewolf/FACELEFT", 3));
        animation.addSequence(State.FACERIGHT, animArray("resources/werewolf/FACERIGHT", 3));
        animation.addSequence(State.MOVELEFT, animArray("resources/werewolf/MOVELEFT", 6));
        animation.addSequence(State.MOVERIGHT, animArray("resources/werewolf/MOVERIGHT", 6));
        animation.addSequence(State.FALLLEFT, animArray("resources/werewolf/FACELEFT", 1));
        animation.addSequence(State.FALLRIGHT, animArray("resources/werewolf/FACERIGHT", 1));
    }

    @Override
    protected void updateIdle() {
        // Werewolves are always patrolling
        enemyState = EnemyState.PATROL;
    }

    @Override
    protected void updatePatrol() {
        movementFactor = PATROL_SPEED;
        
        // Move back and forth
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
        
        moveTowardPlayer();
        
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

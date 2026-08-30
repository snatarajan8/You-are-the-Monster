import java.awt.Rectangle;

public class EnemyBat extends Enemy {

    private static final int PATROL_SPEED = 2;
    private static final int CHASE_SPEED = 4;

    private final int distance;
    private int travelled;
    private int patrolDirection = 1;
    private int bobTimer;

    public EnemyBat(Game game, int x, int y, int height, int width, int patrolDistance) {
        super(game, x, y, 28, 22, 30, 5);
        flying = true;
        distance = Math.max(40, patrolDistance);
        visionRange = 170;
        attackRange = 34;
        attackCooldown = 40;
        visionBox = new Rectangle(x - visionRange, y - visionRange / 2,
            28 + visionRange * 2, 22 + visionRange);
        enemyState = EnemyState.PATROL;
        animation = new SpriteAnimation();
        animation.addSequence(State.MOVELEFT, animArray("resources/Bat/MOVELEFT", 4), 200);
        animation.addSequence(State.MOVERIGHT, animArray("resources/Bat/MOVERIGHT", 4), 200);
        animation.addSequence(State.FACELEFT, animArray("resources/Bat/MOVELEFT", 4), 240);
        animation.addSequence(State.FACERIGHT, animArray("resources/Bat/MOVERIGHT", 4), 240);
        animation.addSequence(State.FALLLEFT, animArray("resources/Bat/MOVELEFT", 4), 240);
        animation.addSequence(State.FALLRIGHT, animArray("resources/Bat/MOVERIGHT", 4), 240);
    }

    @Override
    protected void updateIdle() {
        enemyState = EnemyState.PATROL;
    }

    @Override
    protected void updatePatrol() {
        movementFactor = PATROL_SPEED;
        walk(patrolDirection);
        bobTimer++;
        inputVert = (bobTimer / 15) % 2 == 0 ? 1 : -1;
        travelled += PATROL_SPEED;
        if (travelled >= distance) {
            travelled = 0;
            patrolDirection *= -1;
        }
        if (canSeePlayer()) enemyState = EnemyState.CHASE;
    }

    @Override
    protected void updateChase() {
        movementFactor = CHASE_SPEED;
        if (!canSeePlayer()) {
            enemyState = EnemyState.PATROL;
            return;
        }
        if (targetPlayer != null) {
            double px = targetPlayer.getRectangle().getX();
            double py = targetPlayer.getRectangle().getY();
            walk(px < rectangle.getX() ? -1 : 1);
            inputVert = py < rectangle.getY() - 4 ? -1 : (py > rectangle.getY() + 4 ? 1 : 0);
        }
        if (isPlayerInRange(attackRange)) enemyState = EnemyState.ATTACK;
    }

    @Override
    protected void updateAttack() {
        inputDir = 0;
        inputVert = 0;
        if (currentAttackCooldown <= 0) {
            if (targetPlayer != null && isPlayerInRange(attackRange + 6)) {
                targetPlayer.damageHealth(attackDamage);
            }
            currentAttackCooldown = attackCooldown;
        }
        enemyState = EnemyState.CHASE;
    }
}

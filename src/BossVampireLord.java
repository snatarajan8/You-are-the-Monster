import java.awt.Rectangle;

public class BossVampireLord extends Enemy {

    public static final int BOSS_WIDTH = 44;
    public static final int BOSS_HEIGHT = 60;
    private static final int BOSS_HEALTH = 300;
    private static final int BOSS_DAMAGE = 18;
    private static final int PHASE_2_THRESHOLD = 200;
    private static final int PHASE_3_THRESHOLD = 100;

    private int phase = 1;
    private int patternTimer;
    private int windup;

    public BossVampireLord(Game game, int x, int y) {
        super(game, x, y, BOSS_WIDTH, BOSS_HEIGHT, BOSS_HEALTH, BOSS_DAMAGE);
        movementFactor = 3;
        visionRange = 500;
        attackRange = 58;
        attackCooldown = 40;
        dropsLoot = false;
        visionBox = new Rectangle(x - visionRange, y - visionRange / 2,
            BOSS_WIDTH + visionRange * 2, BOSS_HEIGHT + visionRange);
        enemyState = EnemyState.CHASE;
        loadAnim("resources/player", 6, 3, "FALL");
        tint(0.5, 0.25, -0.15); // blood-crimson lord
        animation.getImageView().setScaleX(1.5);
        animation.getImageView().setScaleY(1.5);
    }

    public int getPhase() { return phase; }

    @Override
    protected void updateIdle() { enemyState = EnemyState.CHASE; }

    @Override
    protected void updatePatrol() { enemyState = EnemyState.CHASE; }

    @Override
    protected void updateChase() {
        moveTowardPlayer();
        if (isPlayerInRange(attackRange)) {
            enemyState = EnemyState.ATTACK;
            windup = (phase == 3) ? 6 : (phase == 2 ? 10 : 16);
        }
    }

    @Override
    protected void updateAttack() {
        inputDir = 0;
        patternTimer++;
        if (windup > 0) {
            windup--;
            // telegraph: lunge on the last few frames
            if (windup < 3) moveTowardPlayer();
            return;
        }
        if (currentAttackCooldown <= 0) {
            if (targetPlayer != null && isPlayerInRange(attackRange + 10)) {
                targetPlayer.damageHealth(attackDamage);
            }
            currentAttackCooldown = attackCooldown;
        }
        enemyState = EnemyState.CHASE;
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (enemyHealth <= PHASE_3_THRESHOLD && phase < 3) {
            phase = 3;
            movementFactor = 5;
            attackCooldown = 22;
        } else if (enemyHealth <= PHASE_2_THRESHOLD && phase < 2) {
            phase = 2;
            movementFactor = 4;
            attackCooldown = 30;
        }
    }

    @Override
    protected void die() {
        super.die();
        game.onBossDefeated();
    }
}

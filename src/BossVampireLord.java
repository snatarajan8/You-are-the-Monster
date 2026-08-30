import java.awt.Rectangle;

public class BossVampireLord extends Enemy {

    public static final int BOSS_WIDTH = 48;
    public static final int BOSS_HEIGHT = 64;
    private static final int BOSS_HEALTH = 300;
    private static final int BOSS_DAMAGE = 20;
    private static final int PHASE_1_THRESHOLD = 200;
    private static final int PHASE_2_THRESHOLD = 100;

    private int phase;
    private int attackPattern;
    private int patternTimer;

    public BossVampireLord(Game game, int x, int y) {
        super(game, x, y, BOSS_WIDTH, BOSS_HEIGHT, BOSS_HEALTH, BOSS_DAMAGE);
        phase = 1;
        attackPattern = 0;
        patternTimer = 0;
        visionRange = 400;
        attackRange = 60;
        visionBox = new Rectangle(x - visionRange, y - visionRange/2, 
            BOSS_WIDTH + visionRange * 2, BOSS_HEIGHT + visionRange);
        enemyState = EnemyState.IDLE;
        dropsLoot = false;
        initAnimation();
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        // Boss uses modified player sprites
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
        patternTimer++;
        
        if (patternTimer >= 60) {
            enemyState = EnemyState.CHASE;
            patternTimer = 0;
        }
    }

    @Override
    protected void updatePatrol() {
        // Boss doesn't patrol
        enemyState = EnemyState.IDLE;
    }

    @Override
    protected void updateChase() {
        if (!canSeePlayer()) {
            enemyState = EnemyState.IDLE;
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
        patternTimer++;
        
        // Different attack patterns based on phase
        switch (phase) {
            case 1:
                // Phase 1: Simple melee attack
                if (patternTimer >= 30) {
                    performAttack();
                    patternTimer = 0;
                    enemyState = EnemyState.CHASE;
                }
                break;
            case 2:
                // Phase 2: Faster attacks
                if (patternTimer >= 20) {
                    performAttack();
                    patternTimer = 0;
                    enemyState = EnemyState.CHASE;
                }
                break;
            case 3:
                // Phase 3: Multiple attacks
                if (patternTimer >= 15) {
                    performAttack();
                    patternTimer = 0;
                    attackPattern = (attackPattern + 1) % 3;
                    if (attackPattern == 0) {
                        enemyState = EnemyState.CHASE;
                    }
                }
                break;
        }
    }

    private void performAttack() {
        if (targetPlayer != null && isPlayerInRange(attackRange)) {
            targetPlayer.damageHealth(attackDamage);
        }
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        
        // Check phase transitions
        if (enemyHealth <= PHASE_2_THRESHOLD && phase < 3) {
            phase = 3;
            attackCooldown = 30;
        } else if (enemyHealth <= PHASE_1_THRESHOLD && phase < 2) {
            phase = 2;
            attackCooldown = 40;
        }
    }

    @Override
    protected void die() {
        super.die();
        // Boss death特殊处理
        System.out.println("VAMPIRE LORD DEFEATED!");
    }

    public int getPhase() {
        return phase;
    }
}

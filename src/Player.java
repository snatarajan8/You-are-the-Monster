import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;

public class Player extends Character {

    public static final int PLAYER_WIDTH = 32;
    public static final int PLAYER_HEIGHT = 32;
    public static final int ATTACK_SPACE = 10;
    private static final int MAX_HEALTH = 100;
    private static final int ATTACK_DAMAGE = 10;

    boolean secondJump;
    private List<Attack> activeAttacks;
    private int attackCooldown;
    private static final int ATTACK_COOLDOWN_FRAMES = 8;
    private List<DamageNumber> damageNumbers;

    public Player (Game game, int x, int y) {
        super(game, x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        health = MAX_HEALTH;
        initAnimation();
        secondJump = false;
        activeAttacks = new ArrayList<>();
        attackCooldown = 0;
        damageNumbers = new ArrayList<>();
    }

    public ImageView getImageView() {
        return animation.getImageView();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    public void heal(int amount) {
        health = Math.min(health + amount, MAX_HEALTH);
    }

    private void initAnimation() {
        animation = new SpriteAnimation();
        animation.addSequence(State.FACELEFT, animArray("resources/player/FACELEFT", 3));
        animation.addSequence(State.FACERIGHT, animArray("resources/player/FACERIGHT", 3));
        animation.addSequence(State.MOVELEFT, animArray("resources/player/MOVELEFT", 6));
        animation.addSequence(State.MOVERIGHT, animArray("resources/player/MOVERIGHT", 6));
        animation.addSequence(State.FALLLEFT, animArray("resources/player/FALLLEFT", 1));
        animation.addSequence(State.FALLRIGHT, animArray("resources/player/FALLRIGHT", 1));
    }

    public void jump() {
        boolean ground = iterateThroughCollision("down", 0) == 0;
        if (ground || secondJump) {
            if (ground) {
                velocity.vertical = jumpFactor;
                secondJump = true;
            } else if (secondJump) {
                velocity.vertical = jumpFactor;
                secondJump = false;
            }
            if (velocity.horizontal < 0) {
                state = State.FALLLEFT;
            } else if (velocity.horizontal > 0) {
                state = State.FALLRIGHT;
            } else {
                state = (state == State.FACELEFT) ? State.FALLLEFT : State.FALLRIGHT;
            }
        }
    }

    public void attack() {
        if (attackCooldown > 0) {
            return;
        }

        Attack attack;
        if (left) {
            attack = new Attack((int) rectangle.getX() - ATTACK_SPACE, (int) rectangle.getY());
        } else {
            attack = new Attack((int) rectangle.getX() + (int) rectangle.getWidth() +
                ATTACK_SPACE, (int) rectangle.getY());
        }
        
        boolean hitEnemy = false;
        for (CollisionUnit item : units) {
            if (attack.checkOverlap(item, "left", 0) != -1) {
                if (item instanceof Enemy && !((Enemy) item).isDead()) {
                    Enemy enemy = (Enemy) item;
                    enemy.takeDamage(ATTACK_DAMAGE);
                    hitEnemy = true;
                    
                    // Create damage number
                    DamageNumber dmgNum = new DamageNumber(
                        enemy.getRectangle().getX() + enemy.getRectangle().getWidth()/2,
                        enemy.getRectangle().getY(),
                        ATTACK_DAMAGE,
                        Color.RED
                    );
                    damageNumbers.add(dmgNum);
                }
            }
        }
        
        game.addUnit(attack);
        activeAttacks.add(attack);
        attackCooldown = ATTACK_COOLDOWN_FRAMES;
    }

    @Override
    public void update() {
        super.update();
        
        // Update cooldowns
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        
        // Update active attacks
        List<Attack> toRemove = new ArrayList<>();
        for (Attack attack : activeAttacks) {
            if (!attack.update()) {
                toRemove.add(attack);
                game.removeUnit(attack);
            }
        }
        activeAttacks.removeAll(toRemove);
        
        // Update damage numbers
        List<DamageNumber> dmgToRemove = new ArrayList<>();
        for (DamageNumber dmgNum : damageNumbers) {
            if (!dmgNum.update()) {
                dmgToRemove.add(dmgNum);
            }
        }
        damageNumbers.removeAll(dmgToRemove);
    }

    public List<DamageNumber> getDamageNumbers() {
        return damageNumbers;
    }
}

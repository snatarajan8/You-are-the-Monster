import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import javafx.scene.image.Image;

import java.util.Set;
//gravity jumping handled in character
//CollisionUnit
//can not solve if new location is an acceptable movement spot, if too close, will walk and part of us will stick into the wall/ground
public abstract class Character extends CollisionUnit {

    protected Game game;
    public State state;
    protected int movementFactor = 10;;
    protected int jumpFactor = -30;
    protected Velocity velocity;
    private int velocityTime;
    private final int fallCap = 20;
    protected boolean left;
    protected int health;
    protected Set<CollisionUnit> units;
    protected boolean keypressed;
    protected SpriteAnimation animation;
    protected boolean invincible;
    protected int invincibilityTimer;
    protected static final int INVINCIBILITY_DURATION = 30;

    public Character(Game game, int x, int y, int height, int width) {
        super(x, y, height, width);
        this.game = game;
        state = State.FACERIGHT;
        velocityTime = 1;
        velocity = new Velocity();
	    left = false;
        keypressed = false;
        invincible = false;
        invincibilityTimer = 0;
        units = game.getCollisionSet();
    }

    public void damageHealth(int damage) {
        if (invincible) return;
        
        health -= damage;
        invincible = true;
        invincibilityTimer = INVINCIBILITY_DURATION;
    }

    public void moveHorizontal(boolean direction, boolean click) {
	//check if is in air?
	//-1 if not in distance,
        //onclick on release, direction = left, click = release
        keypressed = true;
        if ((direction && click) || (!direction && !click)) {
            velocity.horizontal += movementFactor;
        } else {
            velocity.horizontal -= movementFactor;
        }
    }


    public void jump() {
        if (iterateThroughCollision("down", 0) == 0) { //if touching ground
            velocity.vertical = jumpFactor;
        }
	if (velocity.horizontal < 0) {
	    state = State.FALLLEFT;
	} else if (velocity.horizontal > 0) {
	    state = State.FALLRIGHT;
	} else {
	    state = (state == State.FACELEFT) ? State.FALLLEFT : State.FALLRIGHT;
	}
    }


//check if head hits the wall
    public void update() {
        // Update invincibility
        if (invincible) {
            invincibilityTimer--;
            if (invincibilityTimer <= 0) {
                invincible = false;
            }
        }
        
        int moveHorizontally = iterateThroughCollision(mapDir(true, velocity.horizontal), Math.abs(velocity.horizontal));
        int moveVertically = iterateThroughCollision(mapDir(false, velocity.vertical), Math.abs(velocity.vertical));
        velocityTime++;
        if (moveHorizontally == -1) {
            rectangle.setLocation((int) rectangle.getX() + velocity.horizontal, (int) rectangle.getY());
        } else {
            int moveAmount = (int) Math.signum(velocity.horizontal) * moveHorizontally;
            rectangle.setLocation((int) rectangle.getX() + moveAmount, (int) rectangle.getY());
        }

        //vertical calculation
        if (moveVertically == -1) { //free fall
            rectangle.setLocation((int) rectangle.getX(), (int) rectangle.getY() + velocity.vertical);
            velocity.vertical += 2 * velocityTime;
        } else if (moveVertically == 0) { //ground touch
            velocity.vertical = 0;
            velocityTime = 1;
        } else { //will move to ground
            int moveAmount = (int) Math.signum(velocity.vertical) * moveVertically;
            rectangle.setLocation((int) rectangle.getX(), (int) rectangle.getY() + moveAmount);
        }

        if (velocity.vertical > fallCap) {
            velocity.vertical = fallCap;
        }

        //state calculation
        if (velocity.horizontal > 0) {
            left = false;
            state = (velocity.vertical > 0) ? State.FALLRIGHT : State.MOVERIGHT;
        } else if (velocity.horizontal < 0) {
            left = true;
            state = (velocity.vertical > 0) ? State.FALLLEFT : State.MOVELEFT;
        } else {
            state = (velocity.vertical > 0 && left) ? State.FALLLEFT : State.FALLRIGHT;
            if (velocity.vertical == 0) {
            state = left ? State.FACELEFT : State.FACERIGHT;
            }
        }

        // Velocity reset now handled by key release in Game.java

        //speedcap
        if (Math.abs(velocity.horizontal) > movementFactor) {
            velocity.horizontal = (int) Math.signum(velocity.horizontal) * movementFactor;
        }

        animation.playAnimation(state);
        ImageView sprite = animation.getImageView();
        if (sprite.getX() != rectangle.getX() || sprite.getY() != rectangle.getY()) {
            sprite.relocate(rectangle.getX(), rectangle.getY());
        }


    }

    public ImageView getSprite() {
        return animation.getImageView();
    }

    protected int iterateThroughCollision(String direction, int movementFactor) {
        for (CollisionUnit unit : units) {
            if (unit != this) {
                int value = checkOverlap(unit, direction, movementFactor);
                if (value != -1) {
                    return value;
                }
            }
        }
        return -1;
    }

    private String mapDir(boolean dir, int value) {
        if (dir) {
            if (Math.signum(value) == -1) {
                return "left";
            } else {
                return "right";
            }
        } else {
            if (Math.signum(value) == -1) {
                return "up";
            } else {
                return "down";
            }
        }
    }

    protected Image[] animArray(String s, int i) {
        Image[] set = new Image[i];
        for (int k = 0; k < i; k++) {
            String filename = s + (k + 1) + ".png";
            set[k] = new Image(getClass().getResource(filename).toExternalForm());
        }
        return set;
    }

    protected class Velocity {
        public int horizontal;
        public int vertical;

        public Velocity() {
            horizontal = 0;
            vertical = 0;
        }
    }
}

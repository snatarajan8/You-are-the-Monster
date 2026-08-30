import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

import java.util.Set;

// Base class for anything that moves and collides (player + enemies).
// Physics runs at a fixed 30 logic ticks/sec (see Game.TICK_MS); all the
// constants below are tuned for that rate.
public abstract class Character extends CollisionUnit {

    protected Game game;
    public State state;
    protected int movementFactor = 6;      // horizontal speed, px/tick
    protected int jumpFactor = -15;        // initial jump velocity, px/tick
    protected static final int GRAVITY = 1;   // linear downward accel, px/tick^2
    protected final int fallCap = 18;         // terminal fall speed, px/tick
    protected Velocity velocity;
    protected boolean left;
    protected int health;
    protected Set<CollisionUnit> units;
    protected int inputDir;                 // -1 / 0 / +1 desired horizontal move
    protected int inputVert;               // -1 / 0 / +1 desired vertical move (flyers only)
    protected boolean flying;              // true -> ignore gravity, free vertical movement
    protected SpriteAnimation animation;
    protected boolean invincible;
    protected int invincibilityTimer;
    protected int hitStun;                 // frames of knockback where input is ignored
    protected static final int INVINCIBILITY_DURATION = 20;

    public Character(Game game, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.game = game;
        state = State.FACERIGHT;
        velocity = new Velocity();
        left = false;
        inputDir = 0;
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

    /** Set desired horizontal movement: -1 left, 0 stop, +1 right. */
    public void walk(int dir) {
        inputDir = Integer.signum(dir);
    }

    public void applyKnockback(int vx, int vy) {
        velocity.horizontal = vx;
        velocity.vertical = vy;
        hitStun = 6;
    }

    public void jump() {
        if (isOnGround()) {
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

    public boolean isOnGround() {
        java.awt.Rectangle probe = new java.awt.Rectangle(
            rectangle.x, rectangle.y + 2, rectangle.width, rectangle.height);
        for (CollisionUnit u : units) {
            if (u != this && u instanceof Wall && probe.intersects(u.getRectangle())) return true;
        }
        return false;
    }

    /** Is there solid ground just past the feet in this direction? (-1 left / +1 right) */
    protected boolean groundAhead(int dir) {
        int fx = dir < 0 ? rectangle.x - 4 : rectangle.x + rectangle.width + 1;
        java.awt.Rectangle probe = new java.awt.Rectangle(fx, rectangle.y + rectangle.height, 4, 16);
        for (CollisionUnit u : units) {
            if (u instanceof Wall && probe.intersects(u.getRectangle())) return true;
        }
        return false;
    }

    /** Is a wall blocking movement in this direction? */
    protected boolean wallAhead(int dir) {
        int fx = dir < 0 ? rectangle.x - 6 : rectangle.x + rectangle.width + 1;
        java.awt.Rectangle probe = new java.awt.Rectangle(fx, rectangle.y + 4, 6, rectangle.height - 8);
        for (CollisionUnit u : units) {
            if (u instanceof Wall && probe.intersects(u.getRectangle())) return true;
        }
        return false;
    }

    /**
     * Move the box by (dx,dy) and stop it at the first solid wall it hits.
     * @return -1 hit while moving up/left, +1 hit while moving down/right, 0 free
     */
    private int moveAxis(int dx, int dy) {
        rectangle.translate(dx, dy);
        int result = 0;
        for (CollisionUnit u : units) {
            if (u == this || !(u instanceof Wall)) continue;
            java.awt.Rectangle w = u.getRectangle();
            if (!rectangle.intersects(w)) continue;
            if (dx > 0)      { rectangle.x = w.x - rectangle.width;  result = 1; }
            else if (dx < 0) { rectangle.x = w.x + w.width;          result = -1; }
            if (dy > 0)      { rectangle.y = w.y - rectangle.height; result = 1; }
            else if (dy < 0) { rectangle.y = w.y + w.height;         result = -1; }
        }
        return result;
    }

    public void update() {
        if (invincible) {
            invincibilityTimer--;
            if (invincibilityTimer <= 0) invincible = false;
        }

        // --- horizontal intent ---
        if (hitStun > 0) {
            hitStun--;
            velocity.horizontal = (int) (velocity.horizontal * 0.8);
        } else {
            velocity.horizontal = inputDir * movementFactor;
        }

        // --- vertical intent ---
        if (flying && hitStun <= 0) {
            velocity.vertical = inputVert * movementFactor;
        } else if (!flying) {
            velocity.vertical += GRAVITY;      // linear gravity
            if (velocity.vertical > fallCap) velocity.vertical = fallCap;
        }

        // --- resolve movement, one axis at a time ---
        if (velocity.horizontal != 0) {
            moveAxis(velocity.horizontal, 0);
        }
        int vHit = moveAxis(0, velocity.vertical);
        if (vHit != 0) {
            velocity.vertical = 0;
        }

        // --- animation state ---
        boolean airborne = !isOnGround();
        if (velocity.horizontal > 0) {
            left = false;
            state = airborne ? State.FALLRIGHT : State.MOVERIGHT;
        } else if (velocity.horizontal < 0) {
            left = true;
            state = airborne ? State.FALLLEFT : State.MOVELEFT;
        } else if (airborne) {
            state = left ? State.FALLLEFT : State.FALLRIGHT;
        } else {
            state = left ? State.FACELEFT : State.FACERIGHT;
        }

        animation.playAnimation(state);
        ImageView sprite = animation.getImageView();
        if (sprite.getX() != rectangle.getX() || sprite.getY() != rectangle.getY()) {
            sprite.relocate(rectangle.getX(), rectangle.getY());
        }
        // flash while invincible so hits read
        sprite.setOpacity(invincible && (invincibilityTimer % 6 < 3) ? 0.35 : 1.0);
    }

    @Override
    public Node getNode() {
        return animation.getImageView();
    }

    public ImageView getSprite() {
        return animation.getImageView();
    }

    public int getVelX() { return velocity.horizontal; }
    public int getVelY() { return velocity.vertical; }

    /**
     * Wire up a standard 6-state sprite set from a resource folder that follows the
     * MOVE/FACE/FALL + LEFT/RIGHT naming convention.
     */
    protected void loadAnim(String folder, int moveFrames, int faceFrames, String fallBase) {
        animation = new SpriteAnimation();
        animation.addSequence(State.MOVELEFT, animArray(folder + "/MOVELEFT", moveFrames), 480);
        animation.addSequence(State.MOVERIGHT, animArray(folder + "/MOVERIGHT", moveFrames), 480);
        animation.addSequence(State.FACELEFT, animArray(folder + "/FACELEFT", faceFrames), 800);
        animation.addSequence(State.FACERIGHT, animArray(folder + "/FACERIGHT", faceFrames), 800);
        animation.addSequence(State.FALLLEFT, animArray(folder + "/" + fallBase + "LEFT", 1), 400);
        animation.addSequence(State.FALLRIGHT, animArray(folder + "/" + fallBase + "RIGHT", 1), 400);
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

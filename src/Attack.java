import java.util.HashSet;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

/**
 * The player's claw swipe. Lives for a handful of ticks as a real hitbox that
 * damages each enemy once, and draws a visible arc so the swing reads on screen.
 */
public class Attack extends CollisionUnit {

    public static final int ATTACK_WIDTH = 46;
    public static final int ATTACK_HEIGHT = 40;
    private static final int LIFETIME = 6;
    private static final int DAMAGE = 15;

    private final Game game;
    private final boolean facingLeft;
    private final Arc visual;
    private final Set<Enemy> alreadyHit = new HashSet<>();
    private int frame;

    public Attack(Game game, int x, int y, boolean facingLeft) {
        super(x, y, ATTACK_WIDTH, ATTACK_HEIGHT);
        this.game = game;
        this.facingLeft = facingLeft;

        visual = new Arc(ATTACK_WIDTH / 2.0, ATTACK_HEIGHT / 2.0,
            ATTACK_WIDTH / 2.0, ATTACK_HEIGHT / 2.0,
            facingLeft ? 90 : -90, 180);
        visual.setType(ArcType.ROUND);
        visual.setFill(Color.web("#e8e8ff", 0.55));
        visual.setStroke(Color.web("#ffffff", 0.8));
        visual.setStrokeWidth(2);
        visual.setMouseTransparent(true);
    }

    @Override
    public Node getNode() {
        return visual;
    }

    /** @return true while the hitbox is still alive. */
    public boolean update() {
        frame++;
        for (CollisionUnit unit : game.getCollisionSet().toArray(new CollisionUnit[0])) {
            if (unit instanceof Enemy) {
                Enemy e = (Enemy) unit;
                if (!e.isDead() && !alreadyHit.contains(e) && rectangle.intersects(e.getRectangle())) {
                    e.takeDamage(DAMAGE);
                    alreadyHit.add(e);
                    game.onPlayerHitEnemy(e, DAMAGE);
                }
            }
        }
        visual.setOpacity(Math.max(0, 1.0 - (double) frame / LIFETIME));
        return frame < LIFETIME;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}

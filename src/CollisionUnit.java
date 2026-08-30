import java.awt.Rectangle;

import javafx.scene.Node;

/** Anything with an axis-aligned bounding box in world space. */
public class CollisionUnit {

    protected Rectangle rectangle;

    public CollisionUnit(int x, int y, int width, int height) {
        rectangle = new Rectangle(x, y, width, height);
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public boolean intersects(CollisionUnit other) {
        return rectangle.intersects(other.rectangle);
    }

    /** The scene-graph node that draws this unit, or null if it is invisible. */
    public Node getNode() {
        return null;
    }
}

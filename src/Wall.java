import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * A solid platform / boundary. Rendered as a sized stone block with a lit top
 * edge so it reads clearly as ground (the old code stretched a 640px grass PNG
 * over every wall, which never lined up with the collision box).
 */
public class Wall extends CollisionUnit {

    private final Pane node;

    public Wall(int x, int y, int width, int height) {
        super(x, y, width, height);

        Rectangle body = new Rectangle(width, height);
        body.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#3a3a52")),
            new Stop(0.12, Color.web("#2a2a3e")),
            new Stop(1, Color.web("#16161f"))));
        body.setStroke(Color.web("#12121a"));
        body.setStrokeWidth(1);

        Rectangle lip = new Rectangle(width, Math.min(4, height));
        lip.setFill(Color.web("#50506e"));

        node = new Pane(body, lip);
        node.setMouseTransparent(true);
    }

    @Override
    public Node getNode() {
        return node;
    }
}

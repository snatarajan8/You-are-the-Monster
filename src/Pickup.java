import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/** A collectible on the ground. Currently only "meat" (heals the player). */
public class Pickup extends CollisionUnit {

    private final String kind;
    private final ImageView view;
    private int bob;

    public Pickup(String kind, int x, int y) {
        super(x, y, 18, 18);
        this.kind = kind;
        String file = kind.equals("scraps") ? "resources/Pickup/scraps.png" : "resources/Pickup/meat.png";
        Image img = new Image(getClass().getResource(file).toExternalForm());
        view = new ImageView(img);
        view.setFitWidth(18);
        view.setFitHeight(18);
        view.setMouseTransparent(true);
    }

    @Override
    public Node getNode() {
        return view;
    }

    /** Gentle hover so pickups catch the eye. */
    public void update() {
        bob++;
        view.setTranslateY(Math.sin(bob / 6.0) * 3);
    }

    /** Apply the effect to the player. @return points awarded. */
    public int apply(Player player) {
        if (kind.equals("scraps")) {
            player.heal(10);
            return 25;
        }
        player.heal(25);
        return 50;
    }
}

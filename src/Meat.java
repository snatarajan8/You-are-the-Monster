import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Meat extends Pickup {
    private ImageView image;
    private static final int HEAL_AMOUNT = 20;

    public Meat(Pane level, int x, int y, int height, int width) {
        super(level, x, y, height, width);
        image = new ImageView("resources/Pickup/meat.png");
    }

    public void onPickup(Player player) {
        super.onPickup();
        player.heal(HEAL_AMOUNT);
    }

    public ImageView getImage() {
        return image;
    }
}

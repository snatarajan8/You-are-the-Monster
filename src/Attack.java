import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Attack extends CollisionUnit {

    public static final int ATTACK_WIDTH = 30;
    public static final int ATTACK_HEIGHT = 20;
    public static final int ATTACK_DURATION = 4; // frames (200ms at 50ms tick)

    private ImageView view;
    private int frameCount;
    private boolean active;

    public Attack(int x, int y) {
        super(x, y, ATTACK_WIDTH, ATTACK_HEIGHT);
        Image image = new Image(getClass().getResource("resources/player/FACERIGHT1.png").toExternalForm());
        view = new ImageView(image);
        view.setFitWidth(ATTACK_WIDTH);
        view.setFitHeight(ATTACK_HEIGHT);
        frameCount = 0;
        active = true;
    }

    public ImageView getImageView() {
        return view;
    }

    public boolean update() {
        frameCount++;
        return frameCount < ATTACK_DURATION && active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}

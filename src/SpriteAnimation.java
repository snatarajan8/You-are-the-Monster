import java.util.Map;
import java.util.HashMap;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.animation.Timeline;

public class SpriteAnimation extends Transition {

    public static final int ANIMATION_DURATION = 500;

    private ImageView view;
    private Map<State, Image[]> sequences;
    private State currentState;

    public SpriteAnimation() {
        view = new ImageView();
        setCycleCount(Timeline.INDEFINITE);
        setCycleDuration(Duration.millis(ANIMATION_DURATION));
        sequences = new HashMap<>();
        currentState = null;
    }

    public SpriteAnimation(State state, Image[] images) {
        this();
        addSequence(state, images);
    }

    public void addSequence(State state, Image[] images) {
        sequences.put(state, images);
    }

    public void addSequence(State state, Image[] images, int cycleDuration) {
        sequences.put(state, images);
    }

    public void playAnimation(State state) {
        if (state != currentState) {
            currentState = state;
            playFromStart();
        }
    }

    public ImageView getImageView() {
        return view;
    }

    protected void interpolate(double frac) {
        Image[] sequence = sequences.get(currentState);
        if (sequence == null) {
            // Fallback to first available animation
            sequence = sequences.values().iterator().next();
            if (sequence == null) return;
        }
        int index = (int)(frac * sequence.length);
        if (index == sequence.length) {
            index = 0;
        }
        view.setImage(sequence[index]);
    }
}

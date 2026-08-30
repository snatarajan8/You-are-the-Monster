import java.util.Map;
import java.util.HashMap;

import javafx.animation.Transition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.animation.Timeline;

/**
 * Plays a frame sequence for the current {@link State}. Each sequence carries its
 * own cycle length so idle/run/fall can animate at different speeds.
 */
public class SpriteAnimation extends Transition {

    private static final int DEFAULT_DURATION = 500;

    private final ImageView view;
    private final Map<State, Image[]> sequences = new HashMap<>();
    private final Map<State, Integer> durations = new HashMap<>();
    private State currentState;

    public SpriteAnimation() {
        view = new ImageView();
        setCycleCount(Timeline.INDEFINITE);
        setCycleDuration(Duration.millis(DEFAULT_DURATION));
        currentState = null;
    }

    public void addSequence(State state, Image[] images) {
        addSequence(state, images, DEFAULT_DURATION);
    }

    public void addSequence(State state, Image[] images, int cycleDurationMs) {
        sequences.put(state, images);
        durations.put(state, cycleDurationMs);
    }

    public void playAnimation(State state) {
        if (state == currentState) return;
        if (!sequences.containsKey(state)) return;
        currentState = state;
        stop();
        setCycleDuration(Duration.millis(durations.getOrDefault(state, DEFAULT_DURATION)));
        playFromStart();
    }

    public ImageView getImageView() {
        return view;
    }

    @Override
    protected void interpolate(double frac) {
        Image[] sequence = sequences.get(currentState);
        if (sequence == null || sequence.length == 0) return;
        int index = (int) (frac * sequence.length);
        if (index >= sequence.length) index = sequence.length - 1;
        view.setImage(sequence[index]);
    }
}

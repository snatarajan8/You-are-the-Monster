import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Tiny procedural sound layer - no audio assets ship with the game, so each
 * effect is a short synthesised blip. Silently disables itself if the JVM has no
 * usable output mixer (e.g. headless CI).
 */
public class AudioManager {

    private static final float SAMPLE_RATE = 44100f;
    private boolean enabled = true;
    private boolean muted = false;
    private final ExecutorService pool = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "sfx");
        t.setDaemon(true);
        return t;
    });
    private final Map<String, byte[]> clips = new HashMap<>();

    public AudioManager() {
        try {
            clips.put("jump", tone(660, 900, 0.10, 0.35, true));
            clips.put("attack", tone(240, 140, 0.09, 0.30, false));
            clips.put("hit", tone(180, 90, 0.10, 0.32, false));
            clips.put("hurt", tone(140, 70, 0.16, 0.30, false));
            clips.put("pickup", tone(880, 1320, 0.12, 0.30, true));
            clips.put("enemy_down", tone(300, 120, 0.18, 0.28, false));
            clips.put("victory", tone(523, 1047, 0.45, 0.30, true));
            clips.put("game_over", tone(300, 110, 0.6, 0.30, false));
            // probe for a real line; disable cleanly if none
            AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine probe = AudioSystem.getSourceDataLine(fmt)) {
                probe.open(fmt);
            }
        } catch (Throwable t) {
            enabled = false;
            System.err.println("AudioManager: audio disabled (" + t.getClass().getSimpleName() + ")");
        }
    }

    public void play(String name) {
        if (!enabled || muted) return;
        byte[] data = clips.get(name);
        if (data == null) return;
        pool.submit(() -> render(data));
    }

    public void setMuted(boolean m) { this.muted = m; }
    public boolean isMuted() { return muted; }
    public void toggleMute() { muted = !muted; }

    private void render(byte[] data) {
        AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
            line.open(fmt);
            line.start();
            line.write(data, 0, data.length);
            line.drain();
        } catch (Throwable ignored) {
            // a transient mixer failure shouldn't crash the game
        }
    }

    /** Sine sweep from startHz to endHz with a linear attack + exponential decay. */
    private byte[] tone(double startHz, double endHz, double seconds, double gain, boolean rising) {
        int n = (int) (seconds * SAMPLE_RATE);
        byte[] out = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double p = (double) i / n;
            double freq = startHz + (endHz - startHz) * (rising ? p : Math.sqrt(p));
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            double env = Math.min(1, p * 12) * Math.pow(1 - p, 2);
            short s = (short) (Math.sin(phase) * env * gain * Short.MAX_VALUE);
            out[i * 2] = (byte) (s & 0xff);
            out[i * 2 + 1] = (byte) ((s >> 8) & 0xff);
        }
        return out;
    }
}

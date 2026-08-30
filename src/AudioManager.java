import java.applet.Applet;
import java.applet.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private Map<String, AudioClip> sounds;
    private boolean muted;

    public AudioManager() {
        sounds = new HashMap<>();
        muted = false;
        loadSounds();
    }

    private void loadSounds() {
        // Load sound effects - using simple beep sounds as placeholders
        // In a real game, you would load actual .wav or .mp3 files
        try {
            // Create simple tones as placeholders
            // These would be replaced with actual game audio files
            System.out.println("AudioManager: Loading sounds...");
        } catch (Exception e) {
            System.out.println("AudioManager: Could not load sounds: " + e.getMessage());
        }
    }

    public void playSound(String soundName) {
        if (muted) return;
        
        AudioClip clip = sounds.get(soundName);
        if (clip != null) {
            clip.play();
        }
    }

    public void playSoundLoop(String soundName) {
        if (muted) return;
        
        AudioClip clip = sounds.get(soundName);
        if (clip != null) {
            clip.loop();
        }
    }

    public void stopSound(String soundName) {
        AudioClip clip = sounds.get(soundName);
        if (clip != null) {
            clip.stop();
        }
    }

    public void stopAllSounds() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopAllSounds();
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMute() {
        setMuted(!muted);
    }
}

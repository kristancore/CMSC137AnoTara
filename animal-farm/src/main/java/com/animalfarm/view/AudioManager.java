package com.animalfarm.view;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Singleton that manages all background music and sound effects.
 *
 * Tracks:
 * - LandingBackgroundMusic : plays on all screens except the game battle
 * - BattleBackgroundMusic : plays only during the battle
 * - ClickSoundEffect : one-shot played on every mouse click / button press
 */
public class AudioManager {

    private static AudioManager instance;

    private MediaPlayer landingPlayer;
    private MediaPlayer battlePlayer;

    // Pre-warmed pool of click SFX players for zero-latency playback
    private static final int CLICK_POOL_SIZE = 4;
    private final MediaPlayer[] clickPool = new MediaPlayer[CLICK_POOL_SIZE];
    private int clickPoolIndex = 0;

    private AudioManager() {
        landingPlayer = createLoopingPlayer("/audio/LandingBackgroundMusic.mp3");
        battlePlayer = createLoopingPlayer("/audio/BattleBackgroundMusic.mp3");
        initClickPool();
    }

    private void initClickPool() {
        try {
            URL url = getClass().getResource("/audio/ClickSoundEffect.mp3");
            if (url == null)
                return;
            String urlStr = url.toExternalForm();
            for (int i = 0; i < CLICK_POOL_SIZE; i++) {
                MediaPlayer p = new MediaPlayer(new Media(urlStr));
                p.setCycleCount(1);
                // Pre-buffer once ready so first play fires instantly
                p.setOnReady(() -> p.seek(Duration.ZERO));
                clickPool[i] = p;
            }
        } catch (Exception e) {
            System.err.println("AudioManager: failed to init click pool - " + e.getMessage());
        }
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    // ── Background music ────────────────────────────────────────────────────

    /** Start landing/menu music (stops battle music first). */
    public void playLanding() {
        stopBattle();
        if (landingPlayer != null) {
            landingPlayer.seek(Duration.ZERO);
            landingPlayer.play();
        }
    }

    /** Start battle music (stops landing music first). */
    public void playBattle() {
        stopLanding();
        if (battlePlayer != null) {
            battlePlayer.seek(Duration.ZERO);
            battlePlayer.play();
        }
    }

    public void stopLanding() {
        if (landingPlayer != null)
            landingPlayer.stop();
    }

    public void stopBattle() {
        if (battlePlayer != null)
            battlePlayer.stop();
    }

    public void stopAll() {
        stopLanding();
        stopBattle();
    }

    // ── Click SFX ───────────────────────────────────────────────────────────

    /**
     * Play the click SFX instantly using a pre-warmed pool.
     * Round-robins through the pool so rapid clicks don't cut each other off.
     */
    public void playClick() {
        MediaPlayer p = clickPool[clickPoolIndex];
        clickPoolIndex = (clickPoolIndex + 1) % CLICK_POOL_SIZE;
        if (p == null)
            return;
        p.stop();
        p.seek(Duration.ZERO);
        p.play();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private MediaPlayer createLoopingPlayer(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("AudioManager: resource not found - " + resourcePath);
                return null;
            }
            MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
            player.setCycleCount(MediaPlayer.INDEFINITE); // loop forever
            player.setVolume(0.5);
            return player;
        } catch (Exception e) {
            System.err.println("AudioManager: failed to load " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }
}

package com.animalfarm.controller;

import com.animalfarm.model.GameState;
import com.animalfarm.net.GameClient;
import com.animalfarm.view.GameRenderer;
import com.animalfarm.view.HUDRenderer;
import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {
    private final GameState state;
    private final GameRenderer renderer;
    private final HUDRenderer hud;
    private final Runnable onGameOver;
    private final GameClient client; // null in local mode
    private final int localPid;      // for online HUD perspective (0 = local mode)
    private final int otherPid;
    private long lastTime = -1;

    /** Local 2P mode. */
    public GameLoop(GameState state, GameRenderer renderer, HUDRenderer hud, Runnable onGameOver) {
        this.state      = state;
        this.renderer   = renderer;
        this.hud        = hud;
        this.onGameOver = onGameOver;
        this.client     = null;
        this.localPid   = 0;
        this.otherPid   = 0;
    }

    /**
     * Network mode. The GameClient owns the authoritative state; this loop
     * only renders — it does NOT call state.update().
     */
    public GameLoop(GameClient client, GameRenderer renderer, HUDRenderer hud,
                    int localPid, int otherPid, Runnable onGameOver) {
        this.client     = client;
        this.state      = null;
        this.renderer   = renderer;
        this.hud        = hud;
        this.onGameOver = onGameOver;
        this.localPid   = localPid;
        this.otherPid   = otherPid;
    }

    @Override
    public void handle(long now) {
        if (lastTime == -1) {
            lastTime = now;
            return;
        }

        if (client != null) {
            // Network mode: render whatever the server sent us
            lastTime = now;
            GameState netState = client.getState();
            if (netState == null) return;
            renderer.draw(netState);
            hud.draw(netState, localPid, otherPid);
            if (netState.isGameOver()) {
                this.stop();
                if (onGameOver != null) onGameOver.run();
            }
        } else {
            // Local mode
            double delta = (now - lastTime) / 1_000_000_000.0;
            if (delta > 0.05) delta = 0.05;
            lastTime = now;

            state.update(delta);
            renderer.draw(state);
            hud.draw(state);

            if (state.isGameOver()) {
                this.stop();
                if (onGameOver != null) onGameOver.run();
            }
        }
    }
}

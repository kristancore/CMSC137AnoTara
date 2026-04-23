package com.animalfarm.controller;

import com.animalfarm.model.GameState;
import com.animalfarm.view.GameRenderer;
import com.animalfarm.view.HUDRenderer;
import javafx.animation.AnimationTimer;

public class GameLoop extends AnimationTimer {
    private final GameState state;
    private final GameRenderer renderer;
    private final HUDRenderer hud;
    private long lastTime = -1;

    public GameLoop(GameState state, GameRenderer renderer, HUDRenderer hud) {
        this.state = state;
        this.renderer = renderer;
        this.hud = hud;
    }

    @Override
    public void handle(long now) {
        if (lastTime == -1) {
            lastTime = now;
            return;
        }
        double delta = (now - lastTime) / 1_000_000_000.0;
        if (delta > 0.05) delta = 0.05;
        lastTime = now;

        state.update(delta);
        renderer.draw(state);
        hud.draw(state);

        if (state.isGameOver()) {
            this.stop();
        }
    }
}

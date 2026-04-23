package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HUDRenderer {
    private final Label feedLabel;
    private final Label barnHpLabel;
    private final GraphicsContext gc;

    public HUDRenderer(Label feedLabel, Label barnHpLabel, GraphicsContext gc) {
        this.feedLabel = feedLabel;
        this.barnHpLabel = barnHpLabel;
        this.gc = gc;
    }

    public void draw(GameState state) {
        feedLabel.setText("Feed: " + (int) Math.floor(state.getPlayer().getFeedBalance()));
        barnHpLabel.setText("Enemy Barn HP: " + (int) Math.max(0, state.getLane().getEnemyBarn().getHp()));

        if (state.isGameOver() && state.isVictory()) {
            gc.setFill(Color.GREEN);
            gc.setFont(Font.font(72));
            gc.fillText("YOU WIN!", GameConfig.WINDOW_WIDTH / 2.0 - 150, GameConfig.WINDOW_HEIGHT / 2.0);
        }
    }
}

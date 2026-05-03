package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.model.Unit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameRenderer {
    private final GraphicsContext gc;

    public GameRenderer(GraphicsContext gc) {
        this.gc = gc;
    }

    public void draw(GameState state) {
        gc.clearRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        // Lane background
        gc.setFill(Color.web("#8BC34A"));
        gc.fillRect(0, GameConfig.WINDOW_HEIGHT / 2.0 - GameConfig.LANE_HEIGHT / 2.0,
                GameConfig.WINDOW_WIDTH, GameConfig.LANE_HEIGHT);

        // Friendly barn
        gc.setFill(Color.web("#795548"));
        gc.fillRect(state.getLane().getFriendlyBarn().getX(),
                state.getLane().getFriendlyBarn().getY(), 50, 80);

        // Enemy barn
        gc.setFill(Color.web("#F44336"));
        gc.fillRect(state.getLane().getEnemyBarn().getX(),
                state.getLane().getEnemyBarn().getY(), 50, 80);

        // Units
        for (Unit unit : state.getLane().getUnits()) {
            int frameIndex = ((int)(unit.getAnimationTimer() * 8) % 4) + 1;
            javafx.scene.image.Image sprite = SpriteManager.getSprite(unit.getType(), unit.getDirection(), frameIndex);
            
            if (sprite != null) {
                // We draw the sprite covering the logical bounding box
                gc.drawImage(sprite,
                        unit.getX(),
                        unit.getY() - unit.getType().getHeight() / 2.0,
                        unit.getType().getWidth(),
                        unit.getType().getHeight());
            } else {
                gc.setFill(Color.web(unit.getType().getHexColor()));
                gc.fillRect(unit.getX(),
                        unit.getY() - unit.getType().getHeight() / 2.0,
                        unit.getType().getWidth(),
                        unit.getType().getHeight());
            }
        }
    }
}

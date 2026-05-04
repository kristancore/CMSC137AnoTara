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

        // put the bg pic here
        javafx.scene.image.Image bgImage = SpriteManager.getBackgroundImage();
        if (bgImage != null) {
            gc.drawImage(bgImage, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        } else {
            gc.setFill(Color.web("#8BC34A"));
            gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        }

        // Friendly barn (Player 1)
        // If actionTimer > 0, show frame 2 (action), else show frame 1 (idle)
        int p1Frame = state.getLane().getFriendlyBarn().getActionTimer() > 0 ? 2 : 1;
        javafx.scene.image.Image p1Sprite = SpriteManager.getPlayerSprite(1, p1Frame);
        if (p1Sprite != null) {
            gc.drawImage(p1Sprite, state.getLane().getFriendlyBarn().getX() + 10,
                    state.getLane().getFriendlyBarn().getY(), 40, 55);
        } else {
            gc.setFill(Color.web("#795548"));
            gc.fillRect(state.getLane().getFriendlyBarn().getX(),
                    state.getLane().getFriendlyBarn().getY(), 40, 55);
        }

        // Enemy barn (Player 2)
        int p2Frame = state.getLane().getEnemyBarn().getActionTimer() > 0 ? 2 : 1;
        javafx.scene.image.Image p2Sprite = SpriteManager.getPlayerSprite(2, p2Frame);
        if (p2Sprite != null) {
            gc.drawImage(p2Sprite, state.getLane().getEnemyBarn().getX(),
                    state.getLane().getEnemyBarn().getY(), 40, 55);
        } else {
            gc.setFill(Color.web("#F44336"));
            gc.fillRect(state.getLane().getEnemyBarn().getX(),
                    state.getLane().getEnemyBarn().getY(), 40, 55);
        }

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

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

        // Background
        javafx.scene.image.Image bgImage = SpriteManager.getBackgroundImage();
        if (bgImage != null) {
            gc.drawImage(bgImage, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        } else {
            gc.setFill(Color.web("#87CEEB"));
            gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        }

        // Lane dividers
        gc.setStroke(Color.rgb(0, 0, 0, 0.18));
        gc.setLineWidth(1.5);
        for (int i = 1; i < GameConfig.NUM_LANES; i++) {
            double y = i * GameConfig.LANE_HEIGHT;
            gc.strokeLine(0, y, GameConfig.WINDOW_WIDTH, y);
        }

        // Highlight selected rows
        // P1 Highlight (Left side yellowish)
        int sel1 = state.getSelectedRowP1();
        gc.setFill(Color.rgb(255, 255, 100, 0.15));
        gc.fillRect(0, sel1 * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);

        // P2 Highlight (Right side reddish)
        int sel2 = state.getSelectedRowP2();
        gc.setFill(Color.rgb(255, 100, 100, 0.15));
        gc.fillRect(GameConfig.WINDOW_WIDTH / 2.0, sel2 * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);

        // Friendly barn (P1)
        int p1Frame = state.getLane().getFriendlyBarn().getActionTimer() > 0 ? 2 : 1;
        javafx.scene.image.Image p1Sprite = SpriteManager.getPlayerSprite(1, p1Frame);
        double bx = state.getLane().getFriendlyBarn().getX();
        double by = state.getLane().getFriendlyBarn().getY();
        if (p1Sprite != null) {
            gc.drawImage(p1Sprite, bx + 8, by, 44, 60);
        } else {
            gc.setFill(Color.web("#795548"));
            gc.fillRect(bx, by, 44, 60);
        }

        // Enemy barn (P2)
        int p2Frame = state.getLane().getEnemyBarn().getActionTimer() > 0 ? 2 : 1;
        javafx.scene.image.Image p2Sprite = SpriteManager.getPlayerSprite(2, p2Frame);
        double ex = state.getLane().getEnemyBarn().getX();
        double ey = state.getLane().getEnemyBarn().getY();
        if (p2Sprite != null) {
            gc.drawImage(p2Sprite, ex, ey, 44, 60);
        } else {
            gc.setFill(Color.web("#F44336"));
            gc.fillRect(ex, ey, 44, 60);
        }

        // Units
        for (Unit unit : state.getLane().getUnits()) {
            int frameIndex = ((int) (unit.getAnimationTimer() * 8) % 4) + 1;
            javafx.scene.image.Image sprite = SpriteManager.getSprite(unit.getType(), unit.getDirection(), frameIndex);

            double ux = unit.getX();
            double uy = unit.getY() - unit.getType().getHeight() / 2.0;
            int w = unit.getType().getWidth();
            int h = unit.getType().getHeight();

            if (sprite != null) {
                gc.drawImage(sprite, ux, uy, w, h);
            } else {
                gc.setFill(Color.web(unit.getType().getHexColor()));
                gc.fillRect(ux, uy, w, h);
            }
        }
    }
}

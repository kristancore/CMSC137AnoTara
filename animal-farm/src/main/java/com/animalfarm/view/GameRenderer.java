package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.model.Unit;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

public class GameRenderer {
    private final GraphicsContext gc;
    private final int myTeamId; // 1 = normal view, 2 = horizontally mirrored view
    private String[] playerNames; // 1-indexed by game PID, null = don't draw

    public GameRenderer(GraphicsContext gc) {
        this(gc, 1);
    }

    public GameRenderer(GraphicsContext gc, int myTeamId) {
        this.gc       = gc;
        this.myTeamId = myTeamId;
    }

    public void setPlayerNames(String[] names) { this.playerNames = names; }

    public void draw(GameState state) {
        gc.clearRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);

        boolean flip = myTeamId == 2;
        if (flip) {
            gc.save();
            gc.transform(new Affine(-1, 0, GameConfig.WINDOW_WIDTH, 0, 1, 0));
        }

        // Background
        javafx.scene.image.Image bgImage = SpriteManager.getBackgroundImage();
        if (bgImage != null) {
            gc.drawImage(bgImage, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        } else {
            gc.setFill(Color.web("#87CEEB"));
            gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        }


        boolean is4P = state.getMode() == GameState.GameMode.ONLINE_4P;

        // Highlight selected rows
        if (is4P) {
            // Team 1 (P1+P2): left-side highlights, yellow
            gc.setFill(Color.rgb(255, 255, 100, 0.18));
            gc.fillRect(0, state.getSelectedRowP1() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
            gc.setFill(Color.rgb(255, 200, 50, 0.12));
            gc.fillRect(0, state.getSelectedRowP2() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
            // Team 2 (P3+P4): right-side highlights, red
            gc.setFill(Color.rgb(255, 100, 100, 0.18));
            gc.fillRect(GameConfig.WINDOW_WIDTH / 2.0, state.getSelectedRowP3() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
            gc.setFill(Color.rgb(200, 50, 50, 0.12));
            gc.fillRect(GameConfig.WINDOW_WIDTH / 2.0, state.getSelectedRowP4() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
        } else {
            gc.setFill(Color.rgb(255, 255, 100, 0.15));
            gc.fillRect(0, state.getSelectedRowP1() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
            gc.setFill(Color.rgb(255, 100, 100, 0.15));
            gc.fillRect(GameConfig.WINDOW_WIDTH / 2.0, state.getSelectedRowP2() * GameConfig.LANE_HEIGHT, GameConfig.WINDOW_WIDTH / 2.0, GameConfig.LANE_HEIGHT);
        }

        // Barn avatars
        double friendlyX = state.getLane().getFriendlyBarn().getX();
        double enemyX    = state.getLane().getEnemyBarn().getX();
        int p1Frame = state.getLane().getFriendlyBarn().getActionTimer() > 0 ? 2 : 1;
        int p2Frame = state.getLane().getEnemyBarn().getActionTimer() > 0 ? 2 : 1;

        javafx.scene.image.Image p1Sprite = SpriteManager.getPlayerSprite(1, p1Frame);
        javafx.scene.image.Image p2Sprite = SpriteManager.getPlayerSprite(2, p2Frame);

        if (is4P) {
            // In 4P, two players share each team's barn — use selectedRow for each player's
            // avatar Y so they render independently regardless of who controls the barn object.
            double by1 = GameConfig.laneY(state.getSelectedRowP1()) - 27.5;
            double by2 = GameConfig.laneY(state.getSelectedRowP2()) - 27.5;
            double ey1 = GameConfig.laneY(state.getSelectedRowP3()) - 27.5;
            double ey2 = GameConfig.laneY(state.getSelectedRowP4()) - 27.5;

            if (p1Sprite != null) gc.drawImage(p1Sprite, friendlyX + 8, by1, 44, 60);
            else { gc.setFill(Color.web("#795548")); gc.fillRect(friendlyX, by1, 44, 60); }

            if (p1Sprite != null) gc.drawImage(p1Sprite, friendlyX + 8, by2, 44, 60);
            else { gc.setFill(Color.web("#795548")); gc.fillRect(friendlyX, by2, 44, 60); }

            if (p2Sprite != null) gc.drawImage(p2Sprite, enemyX, ey1, 44, 60);
            else { gc.setFill(Color.web("#F44336")); gc.fillRect(enemyX, ey1, 44, 60); }

            if (p2Sprite != null) gc.drawImage(p2Sprite, enemyX, ey2, 44, 60);
            else { gc.setFill(Color.web("#F44336")); gc.fillRect(enemyX, ey2, 44, 60); }
        } else {
            // 2P: one player per team, barn.getY() is authoritative
            double by1 = state.getLane().getFriendlyBarn().getY();
            if (p1Sprite != null) gc.drawImage(p1Sprite, friendlyX + 8, by1, 44, 60);
            else { gc.setFill(Color.web("#795548")); gc.fillRect(friendlyX, by1, 44, 60); }

            double ey1 = state.getLane().getEnemyBarn().getY();
            if (p2Sprite != null) gc.drawImage(p2Sprite, enemyX, ey1, 44, 60);
            else { gc.setFill(Color.web("#F44336")); gc.fillRect(enemyX, ey1, 44, 60); }
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

        if (flip) gc.restore();

        if (playerNames != null) drawPlayerNames(state, flip);
    }

    private void drawPlayerNames(GameState state, boolean flip) {
        boolean is4P = state.getMode() == GameState.GameMode.ONLINE_4P;
        double fw = GameConfig.WINDOW_WIDTH;
        double friendlyX = state.getLane().getFriendlyBarn().getX();
        double enemyX    = state.getLane().getEnemyBarn().getX();

        // Convert canvas-space sprite positions to screen-space for post-flip drawing.
        // Friendly sprites drawn at (friendlyX+8, y), width 44.
        // Enemy sprites drawn at (enemyX, y), width 44.
        double fsx = flip ? fw - (friendlyX + 8) - 44 : friendlyX + 8;
        double esx = flip ? fw - enemyX - 44         : enemyX;

        if (is4P) {
            double by1 = GameConfig.laneY(state.getSelectedRowP1()) - 27.5;
            double by2 = GameConfig.laneY(state.getSelectedRowP2()) - 27.5;
            double ey1 = GameConfig.laneY(state.getSelectedRowP3()) - 27.5;
            double ey2 = GameConfig.laneY(state.getSelectedRowP4()) - 27.5;
            drawTag(1, fsx, by1, true);
            drawTag(2, fsx, by2, true);
            drawTag(3, esx, ey1, false);
            drawTag(4, esx, ey2, false);
        } else {
            double by1 = state.getLane().getFriendlyBarn().getY();
            double ey1 = state.getLane().getEnemyBarn().getY();
            int fp = (myTeamId == 1) ? 1 : 2;
            int ep = (myTeamId == 1) ? 2 : 1;
            drawTag(fp, fsx, by1, myTeamId == 1);
            drawTag(ep, esx, ey1, myTeamId != 1);
        }
    }

    private void drawTag(int pid, double screenX, double screenY, boolean isTeam1) {
        if (playerNames == null || pid < 1 || pid > 4 || playerNames[pid] == null) return;
        String name = playerNames[pid];
        gc.save();
        gc.setFont(javafx.scene.text.Font.font(9));
        double tw = name.length() * 6.0;
        double cx = screenX + 22;
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(cx - tw / 2.0 - 4, screenY - 14, tw + 8, 12, 4, 4);
        gc.setFill(isTeam1 ? Color.web("#ffee44") : Color.web("#ff8888"));
        gc.fillText(name, cx - tw / 2.0, screenY - 4);
        gc.restore();
    }

}

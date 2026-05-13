package com.animalfarm.view;

import com.animalfarm.model.GameState;
import com.animalfarm.model.UnitType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class HUDRenderer {

    // Player 1
    private final Label p1CooldownLabel;
    private final Label p1ScoreLabel;
    private final ImageView p1NextAnimalView;
    private final Label p1NextAnimalName;
    private final Label p1NextCostLabel;

    // Player 2
    private final Label p2CooldownLabel;
    private final Label p2ScoreLabel;
    private final ImageView p2NextAnimalView;
    private final Label p2NextAnimalName;
    private final Label p2NextCostLabel;

    // Global
    private final Label timerLabel;

    public HUDRenderer(
            Label p1CooldownLabel, Label p1ScoreLabel, ImageView p1NextAnimalView, Label p1NextAnimalName, Label p1NextCostLabel,
            Label p2CooldownLabel, Label p2ScoreLabel, ImageView p2NextAnimalView, Label p2NextAnimalName, Label p2NextCostLabel,
            Label timerLabel) {
        this.p1CooldownLabel = p1CooldownLabel;
        this.p1ScoreLabel    = p1ScoreLabel;
        this.p1NextAnimalView = p1NextAnimalView;
        this.p1NextAnimalName = p1NextAnimalName;
        this.p1NextCostLabel  = p1NextCostLabel;

        this.p2CooldownLabel = p2CooldownLabel;
        this.p2ScoreLabel    = p2ScoreLabel;
        this.p2NextAnimalView = p2NextAnimalView;
        this.p2NextAnimalName = p2NextAnimalName;
        this.p2NextCostLabel  = p2NextCostLabel;

        this.timerLabel = timerLabel;
    }

    public void draw(GameState state) {
        // Timer / Sudden Death
        if (state.isSuddenDeath()) {
            timerLabel.setText("SUDDEN DEATH");
            timerLabel.setStyle(timerLabel.getStyle()
                    .replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: #ffff44;")
                    .replaceAll("-fx-font-size: [^;]+;", "-fx-font-size: 10px;"));
        } else {
            int secs = (int) Math.ceil(state.getTimeRemaining());
            int mm = secs / 60;
            int ss = secs % 60;
            timerLabel.setText(String.format("%d:%02d", mm, ss));
            if (state.getTimeRemaining() <= 30) {
                timerLabel.setStyle(timerLabel.getStyle().replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: #ff4c4c;"));
            } else {
                timerLabel.setStyle(timerLabel.getStyle().replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: white;"));
            }
        }

        // P1 (Facing Right = 1)
        updatePlayerHUD(state.getPlayer1(), state.getScoreP1(), state.peekNextP1(),
                p1CooldownLabel, p1ScoreLabel, p1NextAnimalView, p1NextAnimalName, p1NextCostLabel, "SPACE", 1);

        // P2 (Facing Left = -1)
        updatePlayerHUD(state.getPlayer2(), state.getScoreP2(), state.peekNextP2(),
                p2CooldownLabel, p2ScoreLabel, p2NextAnimalView, p2NextAnimalName, p2NextCostLabel, "A", -1);
    }

    private void updatePlayerHUD(com.animalfarm.model.Player player, int score, UnitType next,
                                 Label cooldownLabel, Label scoreLabel, ImageView nextView, Label nextName, Label costLabel, String keyHint, int direction) {
        double cd = player.getSpawnCooldown();
        if (cd <= 0) {
            cooldownLabel.setText("READY");
            cooldownLabel.setStyle(cooldownLabel.getStyle().replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: #88ff88;"));
        } else {
            cooldownLabel.setText(String.format("%.1fs", cd));
            cooldownLabel.setStyle(cooldownLabel.getStyle().replaceAll("-fx-text-fill: [^;]+;", "-fx-text-fill: #ffcc44;"));
        }

        scoreLabel.setText("Score: " + score);

        if (next != null) {
            nextName.setText(capitalize(next.name()));
            costLabel.setText(keyHint);
            Image sprite = SpriteManager.getSprite(next, direction, 1);
            nextView.setImage(sprite);
            nextView.setOpacity(cd <= 0 ? 1.0 : 0.5);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}

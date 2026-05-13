package com.animalfarm.controller;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.model.Unit;
import com.animalfarm.model.UnitType;
import javafx.scene.input.KeyCode;

public class InputHandler {

    public void handleInput(KeyCode key, GameState state) {
        if (state.isGameOver()) return;

        double cooldown = state.isSuddenDeath() ? 0.5 : GameConfig.SPAWN_COOLDOWN;

        // --- Player 1 (Arrows + SPACE) ---
        if (key == KeyCode.UP) {
            if (state.getSelectedRowP1() > 0) {
                int row = state.getSelectedRowP1() - 1;
                state.setSelectedRowP1(row);
                state.getLane().getFriendlyBarn().setY(GameConfig.laneY(row) - 27.5);
            }
        } else if (key == KeyCode.DOWN) {
            if (state.getSelectedRowP1() < GameConfig.NUM_LANES - 1) {
                int row = state.getSelectedRowP1() + 1;
                state.setSelectedRowP1(row);
                state.getLane().getFriendlyBarn().setY(GameConfig.laneY(row) - 27.5);
            }
        } else if (key == KeyCode.SPACE) {
            UnitType unitType = state.peekNextP1();
            if (unitType != null && state.getPlayer1().getSpawnCooldown() <= 0) {
                state.consumeNextP1();
                state.getPlayer1().setSpawnCooldown(cooldown);
                double spawnY = GameConfig.laneY(state.getSelectedRowP1());
                Unit unit = new Unit(unitType, 55, spawnY);
                unit.setDirection(1); // Left to Right
                state.getLane().getUnits().add(unit);
                state.getLane().getFriendlyBarn().triggerAction();
            }
        }

        // --- Player 2 (W/S + A) ---
        if (key == KeyCode.W) {
            if (state.getSelectedRowP2() > 0) {
                int row = state.getSelectedRowP2() - 1;
                state.setSelectedRowP2(row);
                state.getLane().getEnemyBarn().setY(GameConfig.laneY(row) - 27.5);
            }
        } else if (key == KeyCode.S) {
            if (state.getSelectedRowP2() < GameConfig.NUM_LANES - 1) {
                int row = state.getSelectedRowP2() + 1;
                state.setSelectedRowP2(row);
                state.getLane().getEnemyBarn().setY(GameConfig.laneY(row) - 27.5);
            }
        } else if (key == KeyCode.A) {
            UnitType unitType = state.peekNextP2();
            if (unitType != null && state.getPlayer2().getSpawnCooldown() <= 0) {
                state.consumeNextP2();
                state.getPlayer2().setSpawnCooldown(cooldown);
                double spawnY = GameConfig.laneY(state.getSelectedRowP2());
                Unit unit = new Unit(unitType, GameConfig.WINDOW_WIDTH - 55 - unitType.getWidth(), spawnY);
                unit.setDirection(-1); // Right to Left
                state.getLane().getUnits().add(unit);
                state.getLane().getEnemyBarn().triggerAction();
            }
        }
    }
}

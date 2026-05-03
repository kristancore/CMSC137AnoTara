package com.animalfarm.controller;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.model.Unit;
import com.animalfarm.model.UnitType;
import javafx.scene.input.KeyCode;

public class InputHandler {
    public void handleSpawnRequest(KeyCode key, GameState state) {
        UnitType unitType = switch (key) {
            case DIGIT1 -> UnitType.CHICKEN;
            case DIGIT2 -> UnitType.PIG;
            case DIGIT3 -> UnitType.COW;
            case DIGIT4 -> UnitType.SHEEP;
            case DIGIT5 -> UnitType.LLAMA;
            default -> null;
        };
        if (unitType == null) return;

        if (state.getPlayer().getFeedBalance() >= unitType.getFeedCost()
                && state.getPlayer().getSpawnCooldown() <= 0) {
            state.getPlayer().setFeedBalance(state.getPlayer().getFeedBalance() - unitType.getFeedCost());
            state.getPlayer().setSpawnCooldown(GameConfig.SPAWN_COOLDOWN);
            state.getLane().getUnits().add(new Unit(unitType, 50, GameConfig.WINDOW_HEIGHT / 2.0));
            // Trigger the player's throwing/spawning animation
            state.getLane().getFriendlyBarn().triggerAction();
        }
    }
}

package com.animalfarm.controller;

import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.model.Unit;
import com.animalfarm.model.UnitType;
import com.animalfarm.net.GameClient;
import com.animalfarm.net.Protocol;
import javafx.scene.input.KeyCode;

public class InputHandler {

    /**
     * Network mode: forward key events to the server via GameClient.
     * The local player is identified by client.getMyPlayerId().
     * Lane-select changes are applied locally for immediate visual feedback;
     * the server will echo the authoritative row back in the next snapshot.
     *
     * @param key    pressed key
     * @param client connected GameClient
     * @param state  local snapshot state (read for row bounds; lane-select written optimistically)
     */
    public void handleNetworkInput(KeyCode key, GameClient client, GameState state) {
        if (state == null || state.isGameOver()) return;
        int pid = client.getMyPlayerId();
        if (pid < 1) return;

        if (key == KeyCode.UP) {
            int row = Math.max(0, state.getSelectedRow(pid) - 1);
            state.setSelectedRow(pid, row);
            moveBarn(state, pid, row);
            client.sendLaneSelect(row);
        } else if (key == KeyCode.DOWN) {
            int row = Math.min(GameConfig.NUM_LANES - 1, state.getSelectedRow(pid) + 1);
            state.setSelectedRow(pid, row);
            moveBarn(state, pid, row);
            client.sendLaneSelect(row);
        } else if (key == KeyCode.SPACE) {
            client.sendSpawn(state.getSelectedRow(pid));
        }
    }

    public void handleInput(KeyCode key, GameState state) {
        if (state.isGameOver()) return;

        double cooldown = state.isSuddenDeath() ? 1.0 : GameConfig.SPAWN_COOLDOWN;

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
                unit.setDirection(1);
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
                unit.setDirection(-1);
                state.getLane().getUnits().add(unit);
                state.getLane().getEnemyBarn().triggerAction();
            }
        }
    }

    private static int teamId(GameState state, int pid) {
        return (state.getMode() == GameState.GameMode.ONLINE_4P)
                ? (pid <= 2 ? 1 : 2)
                : (pid == 1 ? 1 : 2);
    }

    private static void moveBarn(GameState state, int pid, int row) {
        state.getLane().getBarn(teamId(state, pid)).setY(GameConfig.laneY(row) - 27.5);
    }
}

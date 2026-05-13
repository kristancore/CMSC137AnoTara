package com.animalfarm.model;

public final class GameConfig {

    private GameConfig() {}

    // Deploy cooldown (seconds between each deployment)
    public static final double SPAWN_COOLDOWN  = 1.0;

    // Combat / Base
    public static final double MELEE_RANGE      = 20;
    public static final double ATTACK_INTERVAL  = 1.0;
    public static final double MIN_DAMAGE       = 1;
    public static final double BARN_HP          = 500;

    // Scoring: +1 per animal that crosses to enemy side
    public static final int SCORE_PER_UNIT_KILL = 1;

    // Game timer (seconds)
    public static final double GAME_DURATION = 100.0;

    // Dimensions
    public static final int WINDOW_WIDTH  = 1024;
    public static final int WINDOW_HEIGHT = 600;
    public static final int NUM_LANES     = 6;
    public static final int LANE_HEIGHT   = WINDOW_HEIGHT / NUM_LANES;  // 100px each
    public static final int HUD_HEIGHT    = 80;

    // Lane layout helpers
    public static double laneY(int lane) {
        return lane * LANE_HEIGHT + LANE_HEIGHT / 2.0;
    }

    public static double laneTY(int lane) {
        return lane * LANE_HEIGHT;
    }
}

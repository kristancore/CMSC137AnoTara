package com.animalfarm.model;

public final class GameConfig {

    private GameConfig() {}

    // Economy
    public static final double STARTING_FEED   = 20;
    public static final double FEED_PER_SECOND = 5;
    public static final double MAX_FEED        = 200;
    public static final double SPAWN_COOLDOWN  = 0.5;

    // Combat / Base
    public static final double MELEE_RANGE      = 20;
    public static final double ATTACK_INTERVAL  = 1.0;
    public static final double MIN_DAMAGE       = 1;
    public static final double BARN_HP          = 500;

    // Dimensions
    public static final int WINDOW_WIDTH  = 960;
    public static final int WINDOW_HEIGHT = 540;
    public static final int LANE_HEIGHT   = 200;
    public static final int HUD_HEIGHT    = 140;
}

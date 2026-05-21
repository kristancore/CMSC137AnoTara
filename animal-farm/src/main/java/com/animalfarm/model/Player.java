package com.animalfarm.model;

public class Player {
    private final int playerId;
    private double spawnCooldown;

    public Player() { this(1); }

    public Player(int playerId) {
        this.playerId = playerId;
        this.spawnCooldown = 0;
    }

    public int getPlayerId() { return playerId; }

    public double getSpawnCooldown() { return spawnCooldown; }
    public void setSpawnCooldown(double spawnCooldown) { this.spawnCooldown = spawnCooldown; }

    // Kept for compatibility — feed system removed, always returns 0
    public double getFeedBalance() { return 0; }
    public void setFeedBalance(double ignored) {}
}

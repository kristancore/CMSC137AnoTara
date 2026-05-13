package com.animalfarm.model;

public class Player {
    private double spawnCooldown;

    public Player() {
        this.spawnCooldown = 0;
    }

    public double getSpawnCooldown() { return spawnCooldown; }
    public void setSpawnCooldown(double spawnCooldown) { this.spawnCooldown = spawnCooldown; }

    // Kept for compatibility — feed system removed, always returns 0
    public double getFeedBalance() { return 0; }
    public void setFeedBalance(double ignored) {}
}

package com.animalfarm.model;

public class Player {
    private double feedBalance;
    private double spawnCooldown;

    public Player() {
        this.feedBalance = GameConfig.STARTING_FEED;
        this.spawnCooldown = 0;
    }

    public double getFeedBalance() { return feedBalance; }
    public void setFeedBalance(double feedBalance) { this.feedBalance = feedBalance; }

    public double getSpawnCooldown() { return spawnCooldown; }
    public void setSpawnCooldown(double spawnCooldown) { this.spawnCooldown = spawnCooldown; }
}

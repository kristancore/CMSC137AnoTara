package com.animalfarm.model;

public class Barn {
    private double hp;
    private double x;
    private double y;
    private double actionTimer = 0;

    public Barn(double x, double y) {
        this.hp = GameConfig.BARN_HP;
        this.x = x;
        this.y = y;
    }

    public void takeDamage(double amount) {
        this.hp = Math.max(0, this.hp - amount);
    }

    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getActionTimer() { return actionTimer; }
    
    // Trigger the player's attack/spawn animation
    public void triggerAction() {
        this.actionTimer = 0.3; // Animation lasts 0.3 seconds
    }
    
    // Countdown the timer every frame so the player returns to idle
    public void update(double delta) {
        this.actionTimer = Math.max(0, this.actionTimer - delta);
    }
}

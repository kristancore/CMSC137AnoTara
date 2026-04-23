package com.animalfarm.model;

public class Barn {
    private double hp;
    private double x;
    private double y;

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
}

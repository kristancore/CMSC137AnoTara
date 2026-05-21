package com.animalfarm.model;

public class Unit {
    private UnitType type;
    private double hp;
    private double x;
    private double y;
    private int direction = 1;
    private double attackTimer;
    private double animationTimer = 0;
    private int playerId = 1;

    public Unit(UnitType type, double x, double y) {
        this.type = type;
        this.hp = type.getHp();
        this.x = x;
        this.y = y;
    }

    public UnitType getType() { return type; }
    public void setType(UnitType type) { this.type = type; }

    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public double getAttackTimer() { return attackTimer; }
    public void setAttackTimer(double attackTimer) { this.attackTimer = attackTimer; }

    public double getAnimationTimer() { return animationTimer; }
    public void setAnimationTimer(double animationTimer) { this.animationTimer = animationTimer; }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }

    /** Team 1 moves right (direction=1), Team 2 moves left (direction=-1). */
    public int getTeamId() { return direction == 1 ? 1 : 2; }
}

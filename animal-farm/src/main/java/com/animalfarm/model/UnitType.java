package com.animalfarm.model;

public enum UnitType {
    CHICKEN(50, 5, 0, 80, 5, 30, 30, "#FFD700"),
    PIG(100, 10, 2, 50, 10, 56, 40, "#FFC0CB"),
    COW(200, 20, 5, 30, 20, 68, 60, "#8B4513"),
    SHEEP(250, 15, 10, 25, 15, 60, 55, "#FFFFFF"),
    LLAMA(300, 8, 15, 20, 15, 60, 90, "#C19A6B");

    private final double hp;
    private final double damage;
    private final double armor;
    private final double speed;
    private final double feedCost;
    private final int width;
    private final int height;
    private final String hexColor;

    UnitType(double hp, double damage, double armor, double speed,
             double feedCost, int width, int height, String hexColor) {
        this.hp = hp;
        this.damage = damage;
        this.armor = armor;
        this.speed = speed;
        this.feedCost = feedCost;
        this.width = width;
        this.height = height;
        this.hexColor = hexColor;
    }

    public double getHp() { return hp; }
    public double getDamage() { return damage; }
    public double getArmor() { return armor; }
    public double getSpeed() { return speed; }
    public double getFeedCost() { return feedCost; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getHexColor() { return hexColor; }
}

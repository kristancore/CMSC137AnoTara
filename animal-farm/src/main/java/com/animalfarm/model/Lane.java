package com.animalfarm.model;

import java.util.ArrayList;
import java.util.List;

public class Lane {
    private List<Unit> units;
    private Barn friendlyBarn;
    private Barn enemyBarn;

    public Lane() {
        this.units = new ArrayList<>();
        // Player barn on the left at row 0 initially
        this.friendlyBarn = new Barn(0, GameConfig.laneY(0) - 27.5);
        // Enemy barn on the right side, vertically centered
        this.enemyBarn    = new Barn(GameConfig.WINDOW_WIDTH - 60, GameConfig.laneY(GameConfig.NUM_LANES / 2) - 27.5);
    }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }

    public Barn getFriendlyBarn() { return friendlyBarn; }
    public void setFriendlyBarn(Barn b) { this.friendlyBarn = b; }

    public Barn getEnemyBarn() { return enemyBarn; }
    public void setEnemyBarn(Barn b) { this.enemyBarn = b; }
}

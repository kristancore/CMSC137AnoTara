package com.animalfarm.model;

import java.util.ArrayList;
import java.util.List;

public class Lane {
    private List<Unit> units;
    private Barn friendlyBarn;
    private Barn enemyBarn;

    public Lane() {
        this.units = new ArrayList<>();
        double barnY = GameConfig.WINDOW_HEIGHT / 2.0 - GameConfig.LANE_HEIGHT / 2.0;
        this.friendlyBarn = new Barn(0, barnY);
        this.enemyBarn = new Barn(GameConfig.WINDOW_WIDTH - 50, barnY);
    }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }
    public Barn getFriendlyBarn() { return friendlyBarn; }
    public void setFriendlyBarn(Barn friendlyBarn) { this.friendlyBarn = friendlyBarn; }
    public Barn getEnemyBarn() { return enemyBarn; }
    public void setEnemyBarn(Barn enemyBarn) { this.enemyBarn = enemyBarn; }
}

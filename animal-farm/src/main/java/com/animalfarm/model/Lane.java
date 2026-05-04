package com.animalfarm.model;

import java.util.ArrayList;
import java.util.List;

public class Lane {
    private List<Unit> units;
    private Barn friendlyBarn;
    private Barn enemyBarn;

    public Lane() {
        this.units = new ArrayList<>();
        // player starts at top row
        this.friendlyBarn = new Barn(0, 17.5);
        // put enemy base in middle
        this.enemyBarn = new Barn(GameConfig.WINDOW_WIDTH - 50, 242.5);
    }

    public List<Unit> getUnits() { return units; }
    public void setUnits(List<Unit> units) { this.units = units; }
    public Barn getFriendlyBarn() { return friendlyBarn; }
    public void setFriendlyBarn(Barn friendlyBarn) { this.friendlyBarn = friendlyBarn; }
    public Barn getEnemyBarn() { return enemyBarn; }
    public void setEnemyBarn(Barn enemyBarn) { this.enemyBarn = enemyBarn; }
}

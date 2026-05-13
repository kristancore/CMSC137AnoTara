package com.animalfarm.model;

import java.util.*;

public class GameState {
    private final Lane lane;
    private final Player player1;
    private final Player player2;

    private boolean isGameOver = false;
    private boolean isSuddenDeath = false;
    private int selectedRowP1 = 0;
    private int selectedRowP2 = 0;

    private int scoreP1 = 0;
    private int scoreP2 = 0;

    private double timeRemaining = GameConfig.GAME_DURATION;

    private final Deque<UnitType> queueP1 = new ArrayDeque<>();
    private final Deque<UnitType> queueP2 = new ArrayDeque<>();
    private final Random rng = new Random();

    public GameState() {
        this.lane    = new Lane();
        this.player1 = new Player();
        this.player2 = new Player();
        for (int i = 0; i < 3; i++) {
            queueP1.add(randomUnit());
            queueP2.add(randomUnit());
        }
    }

    public void startSuddenDeath() {
        this.isSuddenDeath = true;
        this.isGameOver = false;
        this.scoreP1 = 0;
        this.scoreP2 = 0;
        this.timeRemaining = 999999; // Practically infinite
        this.lane.getUnits().clear();
        this.player1.setSpawnCooldown(0);
        this.player2.setSpawnCooldown(0);
    }

    public void update(double delta) {
        if (isGameOver) return;

        if (!isSuddenDeath) {
            timeRemaining = Math.max(0, timeRemaining - delta);
            if (timeRemaining <= 0) {
                isGameOver = true;
                return;
            }
        }

        player1.setSpawnCooldown(Math.max(0, player1.getSpawnCooldown() - delta));
        player2.setSpawnCooldown(Math.max(0, player2.getSpawnCooldown() - delta));

        lane.getFriendlyBarn().update(delta);
        lane.getEnemyBarn().update(delta);

        // Process movement
        for (int row = 0; row < GameConfig.NUM_LANES; row++) {
            double laneY = GameConfig.laneY(row);
            List<Unit> laneUnits = new ArrayList<>();
            for (Unit u : lane.getUnits()) {
                if (Math.abs(u.getY() - laneY) < 10) laneUnits.add(u);
            }
            if (laneUnits.isEmpty()) continue;

            laneUnits.sort(Comparator.comparingDouble(Unit::getX));

            List<List<Unit>> groups = new ArrayList<>();
            List<Unit> currentGroup = new ArrayList<>();
            currentGroup.add(laneUnits.get(0));
            groups.add(currentGroup);

            for (int i = 1; i < laneUnits.size(); i++) {
                Unit prev = laneUnits.get(i - 1);
                Unit curr = laneUnits.get(i);
                if (curr.getX() <= prev.getX() + prev.getType().getWidth() + 2) {
                    currentGroup.add(curr);
                } else {
                    currentGroup = new ArrayList<>();
                    currentGroup.add(curr);
                    groups.add(currentGroup);
                }
            }

            for (List<Unit> group : groups) {
                double p1Power = 0, p2Power = 0;
                double p1Speed = 0, p2Speed = 0;
                for (Unit u : group) {
                    if (u.getDirection() == 1) {
                        p1Power += u.getType().getHp();
                        p1Speed = Math.max(p1Speed, u.getType().getSpeed());
                    } else {
                        p2Power += u.getType().getHp();
                        p2Speed = Math.max(p2Speed, u.getType().getSpeed());
                    }
                }

                double moveDir = 0, moveSpeed = 0;
                if (p2Power == 0) { moveDir = 1; moveSpeed = p1Speed; }
                else if (p1Power == 0) { moveDir = -1; moveSpeed = p2Speed; }
                else {
                    if (p1Power > p2Power) { moveDir = 1; moveSpeed = 15; }
                    else if (p2Power > p1Power) { moveDir = -1; moveSpeed = 15; }
                }

                for (Unit u : group) {
                    u.setX(u.getX() + moveSpeed * moveDir * delta);
                    u.setAnimationTimer(u.getAnimationTimer() + delta);
                }
            }
        }

        // Scoring and Removal
        double p1BaseX = lane.getFriendlyBarn().getX();
        double p2BaseX = lane.getEnemyBarn().getX();

        lane.getUnits().removeIf(unit -> {
            if (unit.getDirection() == 1) { // P1 unit
                if (unit.getX() >= p2BaseX - 30) {
                    scoreP1++;
                    lane.getEnemyBarn().triggerAction();
                    if (isSuddenDeath) isGameOver = true; // First to score wins
                    return true;
                }
                if (unit.getX() <= p1BaseX + 10) return true;
            } else { // P2 unit
                if (unit.getX() <= p1BaseX + 30) {
                    scoreP2++;
                    lane.getFriendlyBarn().triggerAction();
                    if (isSuddenDeath) isGameOver = true; // First to score wins
                    return true;
                }
                if (unit.getX() >= p2BaseX + 10) return true;
            }
            return false;
        });
    }

    private UnitType randomUnit() {
        UnitType[] types = UnitType.values();
        return types[rng.nextInt(types.length)];
    }

    public UnitType peekNextP1() { return queueP1.peekFirst(); }
    public UnitType peekNextP2() { return queueP2.peekFirst(); }
    public UnitType consumeNextP1() {
        UnitType unit = queueP1.pollFirst();
        queueP1.add(randomUnit());
        return unit;
    }
    public UnitType consumeNextP2() {
        UnitType unit = queueP2.pollFirst();
        queueP2.add(randomUnit());
        return unit;
    }

    public Lane getLane() { return lane; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isSuddenDeath() { return isSuddenDeath; }
    public int getScoreP1() { return scoreP1; }
    public int getScoreP2() { return scoreP2; }
    public double getTimeRemaining() { return timeRemaining; }
    public int getSelectedRowP1() { return selectedRowP1; }
    public void setSelectedRowP1(int r) { this.selectedRowP1 = r; }
    public int getSelectedRowP2() { return selectedRowP2; }
    public void setSelectedRowP2(int r) { this.selectedRowP2 = r; }
}

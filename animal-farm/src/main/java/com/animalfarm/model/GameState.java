package com.animalfarm.model;

import java.util.*;



public class GameState {

    public enum GameMode { LOCAL_2P, ONLINE_2P, ONLINE_4P }

    private final Lane lane;
    private final Player player1;
    private final Player player2;
    private Player player3;
    private Player player4;

    private GameMode mode = GameMode.LOCAL_2P;

    private boolean isGameOver = false;
    private boolean isSuddenDeath = false;
    private int selectedRowP1 = 0;
    private int selectedRowP2 = 0;
    private int selectedRowP3 = 0;
    private int selectedRowP4 = 0;

    private int scoreP1 = 0;
    private int scoreP2 = 0;

    private double timeRemaining = GameConfig.GAME_DURATION;

    private final Deque<UnitType> queueP1 = new ArrayDeque<>();
    private final Deque<UnitType> queueP2 = new ArrayDeque<>();
    private final Deque<UnitType> queueP3 = new ArrayDeque<>();
    private final Deque<UnitType> queueP4 = new ArrayDeque<>();
    private final Random rng = new Random();

    public GameState() {
        this.lane    = new Lane();
        this.player1 = new Player(1);
        this.player2 = new Player(2);
        for (int i = 0; i < 3; i++) {
            queueP1.add(randomUnit());
            queueP2.add(randomUnit());
        }
    }

    /** Call before starting a 4P online match to activate players 3 and 4. */
    public void activate4P() {
        this.mode = GameMode.ONLINE_4P;
        this.player3 = new Player(3);
        this.player4 = new Player(4);
        for (int i = 0; i < 3; i++) {
            queueP3.add(randomUnit());
            queueP4.add(randomUnit());
        }
    }

    /** Call before starting a 2P online match. */
    public void activateOnline2P() {
        this.mode = GameMode.ONLINE_2P;
    }

    public GameMode getMode() { return mode; }

    /** Returns the Player for the given 1-based id, or null if not active. */
    public Player getPlayer(int id) {
        return switch (id) {
            case 1 -> player1;
            case 2 -> player2;
            case 3 -> player3;
            case 4 -> player4;
            default -> null;
        };
    }

    /** Score for the given player id. In 4P, P1+P2 share scoreP1; P3+P4 share scoreP2. */
    public int getScore(int playerId) {
        return playerId <= 2 ? scoreP1 : scoreP2;
    }

    /**
     * Authoritative spawn: dequeues the next unit for the player and adds it to the lane.
     * Returns false if on cooldown or player not active.
     */
    public boolean spawnUnit(int playerId, int selectedRow) {
        Player player = getPlayer(playerId);
        if (player == null || player.getSpawnCooldown() > 0) return false;

        double cooldown = (mode == GameMode.ONLINE_4P)
                ? GameConfig.SPAWN_COOLDOWN_4P
                : GameConfig.SPAWN_COOLDOWN;

        UnitType unitType = switch (playerId) {
            case 1 -> consumeNextP1();
            case 2 -> consumeNextP2();
            case 3 -> consumeNextP3();
            case 4 -> consumeNextP4();
            default -> null;
        };
        if (unitType == null) return false;

        player.setSpawnCooldown(cooldown);

        // 2P: P1=team1, P2=team2.  4P: P1+P2=team1, P3+P4=team2.
        int teamId = (mode == GameMode.ONLINE_4P)
                ? (playerId <= 2 ? 1 : 2)
                : (playerId == 1 ? 1 : 2);
        int direction = teamId == 1 ? 1 : -1;
        double spawnX = direction == 1
                ? 55
                : GameConfig.WINDOW_WIDTH - 55 - unitType.getWidth();
        double spawnY = GameConfig.laneY(selectedRow);

        Unit unit = new Unit(unitType, spawnX, spawnY);
        unit.setDirection(direction);
        unit.setPlayerId(playerId);
        lane.getUnits().add(unit);
        lane.getBarn(teamId).triggerAction();
        return true;
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
        if (player3 != null) player3.setSpawnCooldown(0);
        if (player4 != null) player4.setSpawnCooldown(0);
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
        if (player3 != null) player3.setSpawnCooldown(Math.max(0, player3.getSpawnCooldown() - delta));
        if (player4 != null) player4.setSpawnCooldown(Math.max(0, player4.getSpawnCooldown() - delta));

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
    public UnitType peekNextP3() { return queueP3.peekFirst(); }
    public UnitType peekNextP4() { return queueP4.peekFirst(); }

    public UnitType consumeNextP1() { UnitType u = queueP1.pollFirst(); queueP1.add(randomUnit()); return u; }
    public UnitType consumeNextP2() { UnitType u = queueP2.pollFirst(); queueP2.add(randomUnit()); return u; }
    public UnitType consumeNextP3() { UnitType u = queueP3.pollFirst(); queueP3.add(randomUnit()); return u; }
    public UnitType consumeNextP4() { UnitType u = queueP4.pollFirst(); queueP4.add(randomUnit()); return u; }

    /** Peek the next unit for any player by id. */
    public UnitType peekNext(int playerId) {
        return switch (playerId) {
            case 1 -> peekNextP1();
            case 2 -> peekNextP2();
            case 3 -> peekNextP3();
            case 4 -> peekNextP4();
            default -> null;
        };
    }

    public Lane getLane() { return lane; }
    public Player getPlayer1() { return player1; }
    public Player getPlayer2() { return player2; }
    public Player getPlayer3() { return player3; }
    public Player getPlayer4() { return player4; }
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean v) { this.isGameOver = v; }
    public boolean isSuddenDeath() { return isSuddenDeath; }
    public int getScoreP1() { return scoreP1; }
    public int getScoreP2() { return scoreP2; }
    public void setScoreP1(int v) { this.scoreP1 = v; }
    public void setScoreP2(int v) { this.scoreP2 = v; }
    public double getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(double v) { this.timeRemaining = v; }
    public int getSelectedRowP1() { return selectedRowP1; }
    public void setSelectedRowP1(int r) { this.selectedRowP1 = r; }
    public int getSelectedRowP2() { return selectedRowP2; }
    public void setSelectedRowP2(int r) { this.selectedRowP2 = r; }
    public int getSelectedRowP3() { return selectedRowP3; }
    public void setSelectedRowP3(int r) { this.selectedRowP3 = r; }
    public int getSelectedRowP4() { return selectedRowP4; }
    public void setSelectedRowP4(int r) { this.selectedRowP4 = r; }

    public int getSelectedRow(int playerId) {
        return switch (playerId) {
            case 1 -> selectedRowP1;
            case 2 -> selectedRowP2;
            case 3 -> selectedRowP3;
            case 4 -> selectedRowP4;
            default -> 0;
        };
    }

    public void setSelectedRow(int playerId, int row) {
        switch (playerId) {
            case 1 -> selectedRowP1 = row;
            case 2 -> selectedRowP2 = row;
            case 3 -> selectedRowP3 = row;
            case 4 -> selectedRowP4 = row;
        }
    }
}

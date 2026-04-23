package com.animalfarm.model;

public class GameState {
    private Lane lane;
    private Player player;
    private boolean isGameOver = false;
    private boolean isVictory = false;

    public GameState() {
        this.lane = new Lane();
        this.player = new Player();
    }

    public void update(double delta) {
        player.setFeedBalance(Math.min(
            player.getFeedBalance() + GameConfig.FEED_PER_SECOND * delta,
            GameConfig.MAX_FEED
        ));
        player.setSpawnCooldown(Math.max(0, player.getSpawnCooldown() - delta));

        for (Unit unit : lane.getUnits()) {
            unit.setX(unit.getX() + unit.getType().getSpeed() * unit.getDirection() * delta);
        }

        for (Unit unit : lane.getUnits()) {
            if (unit.getX() >= lane.getEnemyBarn().getX()) {
                lane.getEnemyBarn().takeDamage(unit.getType().getDamage() * delta);
            }
        }

        if (lane.getEnemyBarn().getHp() <= 0) {
            isGameOver = true;
            isVictory = true;
        }
    }

    public Lane getLane() { return lane; }
    public void setLane(Lane lane) { this.lane = lane; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public boolean isGameOver() { return isGameOver; }
    public void setGameOver(boolean isGameOver) { this.isGameOver = isGameOver; }
    public boolean isVictory() { return isVictory; }
    public void setVictory(boolean isVictory) { this.isVictory = isVictory; }
}

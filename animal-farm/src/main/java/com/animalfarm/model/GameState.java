package com.animalfarm.model;

public class GameState {
    private Lane lane;
    private Player player;
    private boolean isGameOver = false;
    private boolean isVictory = false;
    private int selectedRow = 0;

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
        
        // Update the barns so their action timers count down
        lane.getFriendlyBarn().update(delta);
        lane.getEnemyBarn().update(delta);

        for (Unit unit : lane.getUnits()) {
            unit.setX(unit.getX() + unit.getType().getSpeed() * unit.getDirection() * delta);
            unit.setAnimationTimer(unit.getAnimationTimer() + delta);
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
    public int getSelectedRow() { return selectedRow; }
    public void setSelectedRow(int selectedRow) { this.selectedRow = selectedRow; }
}

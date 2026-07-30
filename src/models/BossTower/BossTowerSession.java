package models.BossTower;

import boss.boss_manifest.BossTower.BossTowerBoss;
import map.Zone;
import nro.player.Player;

public class BossTowerSession {

    public final long playerId;
    public final Player player;
    public final Zone zone;
    public final long startedAt;
    public int currentFloor;
    public int maxFloor;
    public BossTowerBoss currentBoss;
    private boolean active = true;

    public BossTowerSession(Player player, Zone zone, int startFloor) {
        this.player = player;
        this.playerId = player.id;
        this.zone = zone;
        this.currentFloor = startFloor;
        this.maxFloor = Math.max(0, startFloor - 1);
        this.startedAt = System.currentTimeMillis();
    }

    public int elapsedSeconds() {
        return (int) Math.max(0, (System.currentTimeMillis() - startedAt) / 1000);
    }

    public boolean isActive() {
        return active;
    }

    public void close() {
        this.active = false;
    }
}

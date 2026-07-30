package models.BossTower;

public class BossTowerWeeklyRecord {

    public int playerId;
    public String playerName;
    public String weekKey;
    public int maxFloor;
    public int bestTime;
    public boolean claimed;
    public int claimedFloor;
    public boolean topClaimed;

    public BossTowerWeeklyRecord() {
    }

    public BossTowerWeeklyRecord(int playerId, String playerName, String weekKey, int maxFloor, int bestTime, boolean claimed) {
        this(playerId, playerName, weekKey, maxFloor, bestTime, claimed, claimed ? maxFloor : 0, false);
    }

    public BossTowerWeeklyRecord(int playerId, String playerName, String weekKey, int maxFloor, int bestTime, boolean claimed, int claimedFloor, boolean topClaimed) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.weekKey = weekKey;
        this.maxFloor = maxFloor;
        this.bestTime = bestTime;
        this.claimed = claimed;
        this.claimedFloor = Math.max(0, claimedFloor);
        this.topClaimed = topClaimed;
    }
}

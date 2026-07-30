package player.dailyGift;

import nro.player.Player;

public class DailyGiftService {

    private static final byte[] DAILY_GIFT_IDS = new byte[]{0, 1, 2, 3};

    public static boolean checkDailyGift(Player player, byte id) {
        synchronized (player.dailyGiftData) {
            for (DailyGiftData data : player.dailyGiftData) {
                if (data.id == id && !data.daNhan) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void updateDailyGift(Player player, byte id) {
        synchronized (player.dailyGiftData) {
            for (DailyGiftData data : player.dailyGiftData) {
                if (data.id == id) {
                    data.daNhan = true;
                }
            }
        }
    }

    public static boolean checkAndClaimDailyGift(Player player, byte id) {
        synchronized (player.dailyGiftData) {
            boolean exists = false;
            boolean canClaim = false;
            for (DailyGiftData data : player.dailyGiftData) {
                if (data.id == id) {
                    exists = true;
                    if (!data.daNhan) {
                        canClaim = true;
                    }
                    data.daNhan = true;
                }
            }
            if (!exists) {
                DailyGiftData data = new DailyGiftData();
                data.id = id;
                data.daNhan = true;
                player.dailyGiftData.add(data);
                return true;
            }
            return canClaim;
        }
    }

    public static void ensureDailyGift(Player player, byte id) {
        synchronized (player.dailyGiftData) {
            for (DailyGiftData data : player.dailyGiftData) {
                if (data.id == id) {
                    return;
                }
            }
            DailyGiftData data = new DailyGiftData();
            data.id = id;
            data.daNhan = false;
            player.dailyGiftData.add(data);
        }
    }

    public static void ensureDailyGifts(Player player) {
        for (byte id : DAILY_GIFT_IDS) {
            ensureDailyGift(player, id);
        }
    }

    public static void addAndReset(Player player) {
        if (player.dailyGiftData != null) {
            player.dailyGiftData.clear();
        }
        for (byte id : DAILY_GIFT_IDS) {
            DailyGiftData data = new DailyGiftData();
            data.id = id;
            data.daNhan = false;
            player.dailyGiftData.add(data);
        }
    }

}

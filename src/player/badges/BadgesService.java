package player.badges;

import nro.player.Player;
import nro.services.Service;

public class BadgesService {

    private static final int BADGE_DISPLAY_SECONDS = 6;

    public static void turnOnBadges(Player player, int id) {
        if (player.dataBadges != null) {
            for (BadgesData data : player.dataBadges) {
                if (data.idBadGes == id) {
                    data.isUse = true;
                } else {
                    data.isUse = false;
                }
            }
        }
        player.badges.idBadges = id;
        player.badges.lastTimeSendBadges = 0;
        Service.gI().sendBadgesPlayer(player, BADGE_DISPLAY_SECONDS, id);
    }

}

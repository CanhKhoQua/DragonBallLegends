package models.BossTower;

import item.Item;
import jdbc.daos.NDVSqlFetcher;
import nro.player.Player;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;
import player.badges.BadgesData;

public class BossTowerRewardService {

    public static final int BADGE_BAT_BAI = 1007;
    public static final int BADGE_TRUM_CUOI = 1008;
    public static final short XU_NRO = 1705;
    public static final short THOI_VANG = 457;

    private BossTowerRewardService() {
    }

    public static int getWeeklyXuByFloor(int floor) {
        if (floor >= 100) {
            return 650;
        }
        if (floor >= 90) {
            return 530;
        }
        if (floor >= 80) {
            return 420;
        }
        if (floor >= 70) {
            return 325;
        }
        if (floor >= 60) {
            return 245;
        }
        if (floor >= 50) {
            return 180;
        }
        if (floor >= 40) {
            return 125;
        }
        if (floor >= 30) {
            return 80;
        }
        if (floor >= 20) {
            return 45;
        }
        if (floor >= 10) {
            return 20;
        }
        return 0;
    }

    public static int getWeeklyGoldBarByFloor(int floor) {
        if (floor >= 100) {
            return 420;
        }
        if (floor >= 90) {
            return 355;
        }
        if (floor >= 80) {
            return 295;
        }
        if (floor >= 70) {
            return 240;
        }
        if (floor >= 60) {
            return 190;
        }
        if (floor >= 50) {
            return 145;
        }
        if (floor >= 40) {
            return 105;
        }
        if (floor >= 30) {
            return 70;
        }
        if (floor >= 20) {
            return 40;
        }
        if (floor >= 10) {
            return 20;
        }
        return 0;
    }

    public static int getWeeklyTopBonusXu(int rank) {
        if (rank == 1) {
            return 150;
        }
        if (rank <= 3) {
            return 100;
        }
        if (rank <= 10) {
            return 50;
        }
        if (rank <= 50) {
            return 20;
        }
        return 0;
    }

    public static int getWeeklyTopBonusGoldBar(int rank) {
        if (rank == 1) {
            return 100;
        }
        if (rank <= 3) {
            return 50;
        }
        if (rank <= 10) {
            return 25;
        }
        if (rank <= 50) {
            return 10;
        }
        return 0;
    }

    public static boolean grantXu(Player player, int quantity) {
        if (player == null || quantity <= 0) {
            return false;
        }
        Item xu = ItemService.gI().createNewItem(XU_NRO, quantity);
        boolean added = InventoryService.gI().addItemBag(player, xu);
        if (added) {
            InventoryService.gI().sendItemBag(player);
        }
        return added;
    }

    public static boolean grantXuToMail(Player player, int quantity) {
        if (player == null || quantity <= 0) {
            return false;
        }
        Item xu = ItemService.gI().createNewItem(XU_NRO, quantity);
        player.inventory.itemsMailBox.add(xu);
        return NDVSqlFetcher.updateMailBox(player);
    }

    public static boolean grantMilestoneRewardToMail(Player player, int xuQuantity, int goldBarQuantity) {
        if (player == null || xuQuantity <= 0 && goldBarQuantity <= 0) {
            return false;
        }
        Item xu = null;
        Item goldBar = null;
        if (xuQuantity > 0) {
            xu = ItemService.gI().createNewItem(XU_NRO, xuQuantity);
            player.inventory.itemsMailBox.add(xu);
        }
        if (goldBarQuantity > 0) {
            goldBar = ItemService.gI().createNewItem(THOI_VANG, goldBarQuantity);
            goldBar.addOptionParam(30, 1);
            player.inventory.itemsMailBox.add(goldBar);
        }
        boolean updated = NDVSqlFetcher.updateMailBox(player);
        if (!updated) {
            if (xu != null) {
                player.inventory.itemsMailBox.remove(xu);
            }
            if (goldBar != null) {
                player.inventory.itemsMailBox.remove(goldBar);
            }
        }
        return updated;
    }

    public static boolean grantXuToBagOrMail(Player player, int quantity) {
        if (player == null || quantity <= 0) {
            return false;
        }
        if (InventoryService.gI().getCountEmptyBag(player) <= 0) {
            return grantXuToMail(player, quantity);
        }
        return grantXu(player, quantity);
    }

    public static boolean grantTopReward(Player player, int xuQuantity, int goldBarQuantity) {
        if (player == null || xuQuantity <= 0 || goldBarQuantity <= 0) {
            return false;
        }
        Item xu = ItemService.gI().createNewItem(XU_NRO, xuQuantity);
        Item goldBar = ItemService.gI().createNewItem(THOI_VANG, goldBarQuantity);
        goldBar.addOptionParam(30, 1);
        if (InventoryService.gI().getCountEmptyBag(player) >= 2) {
            boolean addedXu = InventoryService.gI().addItemBag(player, xu);
            boolean addedGold = InventoryService.gI().addItemBag(player, goldBar);
            if (addedXu && addedGold) {
                InventoryService.gI().sendItemBag(player);
                return true;
            }
            return false;
        }
        player.inventory.itemsMailBox.add(xu);
        player.inventory.itemsMailBox.add(goldBar);
        boolean updated = NDVSqlFetcher.updateMailBox(player);
        if (!updated) {
            player.inventory.itemsMailBox.remove(xu);
            player.inventory.itemsMailBox.remove(goldBar);
        }
        return updated;
    }

    public static void grantBatBaiBadge(Player player) {
        grantBadge(player, BADGE_BAT_BAI, 7);
    }

    public static void grantTrumCuoiBadge(Player player) {
        grantBadge(player, BADGE_TRUM_CUOI, 7);
    }

    public static void grantBadge(Player player, int idEffect, int days) {
        if (player == null || days <= 0) {
            return;
        }
        long expireTime = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L;
        if (player.dataBadges != null) {
            for (BadgesData badge : player.dataBadges) {
                if (badge.idBadGes == idEffect) {
                    badge.isUse = true;
                    player.badges.idBadges = idEffect;
                    Service.gI().sendBadgesPlayer(player, 5, idEffect);
                    if (badge.timeofUseBadges <= System.currentTimeMillis()) {
                        badge.timeofUseBadges = expireTime;
                        Service.gI().sendThongBao(player, "Đã nhận lại danh hiệu " + days + " ngày");
                    } else {
                        Service.gI().sendThongBao(player, "Bạn đang có danh hiệu này, không gia hạn thêm");
                    }
                    return;
                }
                badge.isUse = false;
            }
        }
        new BadgesData(player, idEffect, days);
        player.badges.idBadges = idEffect;
        Service.gI().sendBadgesPlayer(player, 5, idEffect);
        Service.gI().sendThongBao(player, "Đã nhận danh hiệu " + days + " ngày");
    }
}

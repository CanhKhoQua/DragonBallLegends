package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import nro.player.Player;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;
import utils.Util;

public class PhanRaTrangBiKichHoat {

    public class PhanRaTrangBi {

        private static final int GOLD_PHAN_RA = 100_000_000;
        private static final int RATIO_PHAN_RA = 100;
        private static final int DA_NGU_SAC_ID = 674;
        private static final int DA_NGU_SAC_REWARD_DTL = 1;
        private static final int DA_NGU_SAC_REWARD_DHD = 5;
        private static final int MAX_ITEM_PHAN_RA = 10;

        public static void showInfoCombine(Player player) {
            if (player.combine.itemsCombine.isEmpty() || player.combine.itemsCombine.size() > MAX_ITEM_PHAN_RA) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Cần đặt từ 1 đến " + MAX_ITEM_PHAN_RA + " đồ Thần Linh hoặc Hủy Diệt!", "Đóng");
                return;
            }

            if (!isValidItems(player)) {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                        "Chỉ có thể phân rã đồ Thần Linh hoặc Hủy Diệt!", "Đóng");
                return;
            }

            int itemCount = player.combine.itemsCombine.size();
            int goldPhanRa = getGoldPhanRa(player);
            int daNguSacReward = getDaNguSacReward(player);
            player.combine.goldCombine = goldPhanRa;
            player.combine.ratioCombine = RATIO_PHAN_RA;

            String npcSay = "|2|Tỉ lệ thành cộng: " + RATIO_PHAN_RA + "%\n"
                    + "|2|Số trang bị: " + itemCount + "\n"
                    + "|2|Nhận: " + daNguSacReward + " Đá ngũ sắc\n"
                    + "|2|Cần: " + Util.numberToMoney(goldPhanRa) + " vàng\n";
            if (player.inventory.gold < goldPhanRa) {
                npcSay += "|7|Còn thiếu " + Util.powerToString(goldPhanRa - player.inventory.gold) + " vàng\n";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
            } else {
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Phân rã\n" + Util.numberToMoney(goldPhanRa) + " vàng", "Từ chối");
            }
        }

        public static void ThucHienPhanRa(Player player) {
            if (player.combine.itemsCombine.isEmpty() || player.combine.itemsCombine.size() > MAX_ITEM_PHAN_RA) {
                Service.gI().sendThongBao(player, "Cần đặt từ 1 đến " + MAX_ITEM_PHAN_RA + " đồ Thần Linh hoặc Hủy Diệt!");
                return;
            }

            if (!isValidItems(player)) {
                Service.gI().sendThongBao(player, "Chỉ có thể phân rã đồ Thần Linh hoặc Hủy Diệt!");
                return;
            }
            int goldPhanRa = getGoldPhanRa(player);
            if (player.inventory.gold < goldPhanRa) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện!");
                return;
            }
            if (InventoryService.gI().getCountEmptyBag(player) <= 0 && InventoryService.gI().findItemBag(player, DA_NGU_SAC_ID) == null) {
                Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống!");
                return;
            }

            int daNguSacReward = getDaNguSacReward(player);
            player.inventory.gold -= goldPhanRa;
            for (Item item : player.combine.itemsCombine) {
                InventoryService.gI().subQuantityItemsBag(player, item, 1);
            }

            if (Util.isTrue(RATIO_PHAN_RA, 100)) {
                Item reward = InventoryService.gI().findItemBag(player, DA_NGU_SAC_ID);
                if (reward == null) {
                    reward = ItemService.gI().createNewItem((short) DA_NGU_SAC_ID);
                    reward.quantity = 0;
                    if (!InventoryService.gI().addItemBag(player, reward)) {
                        Service.gI().sendThongBao(player, "Không thể thêm Đá ngũ sắc vào hành trang!");
                        return;
                    }
                    reward = InventoryService.gI().findItemBag(player, DA_NGU_SAC_ID);
                    if (reward == null) {
                        Service.gI().sendThongBao(player, "Không tìm thấy Đá ngũ sắc trong hành trang sau khi thêm!");
                        return;
                    }
                }
                reward.quantity += daNguSacReward;
                CombineService.gI().sendEffectSuccessCombine(player);
                Service.gI().sendThongBao(player, "Phân rã thành công, nhận được " + daNguSacReward + " Đá ngũ sắc!");
            } else {
                CombineService.gI().sendEffectFailCombine(player);
                Service.gI().sendThongBao(player, "Phân rã thất bại!");
            }

            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }

        private static boolean isValidItem(Item item) {
            return item != null && item.template != null && (item.isDTL() || item.isDHD());
        }

        private static int getDaNguSacReward(Item item) {
            return item != null && item.isDHD() ? DA_NGU_SAC_REWARD_DHD : DA_NGU_SAC_REWARD_DTL;
        }

        private static boolean isValidItems(Player player) {
            for (Item item : player.combine.itemsCombine) {
                if (!isValidItem(item)) {
                    return false;
                }
            }
            return true;
        }

        private static int getDaNguSacReward(Player player) {
            int reward = 0;
            for (Item item : player.combine.itemsCombine) {
                reward += getDaNguSacReward(item);
            }
            return reward;
        }

        private static int getGoldPhanRa(Player player) {
            return GOLD_PHAN_RA * player.combine.itemsCombine.size();
        }
    }
}

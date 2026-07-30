package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import nro.player.Player;
import nro.services.InventoryService;
import nro.services.Service;
import utils.Util;

public class VinhVienCaiTrang {

    private static final int DA_NGU_SAC_ID = 674;
    private static final int COST_DA_NGU_SAC = 100;
    private static final long COST_GOLD = 1_000_000_000L;

    private VinhVienCaiTrang() {
    }

    public static void showInfoCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null || player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBaoOK(player, "Cần 1 cải trang có hạn sử dụng và Đá ngũ sắc");
            return;
        }

        Item costume = null;
        Item daNguSac = null;
        for (Item item : player.combine.itemsCombine) {
            if (isValidCostume(item)) {
                costume = item;
            } else if (item != null && item.isNotNullItem() && item.template != null && item.template.id == DA_NGU_SAC_ID) {
                daNguSac = item;
            }
        }

        if (costume == null) {
            Service.gI().sendThongBaoOK(player, "Chỉ có thể vĩnh viễn cải trang có hạn sử dụng");
            return;
        }
        if (!hasExpiryDate(costume)) {
            Service.gI().sendThongBaoOK(player, "Cải trang này đã là vĩnh viễn rồi");
            return;
        }

        int requiredDa = getRequiredDaNguSac(costume);
        long requiredGold = getRequiredGold(costume);
        StringBuilder npcSay = new StringBuilder();
        npcSay.append("|2|Vĩnh viễn cải trang\n");
        npcSay.append("|1|").append(costume.template.name).append("\n");
        for (Item.ItemOption option : costume.itemOptions) {
            npcSay.append(option.getOptionString()).append("\n");
        }
        npcSay.append("\n|7|Sau khi làm phép sẽ xoá hạn sử dụng và giữ nguyên toàn bộ chỉ số.");
        npcSay.append("\n|2|Cần ").append(requiredDa).append(" Đá ngũ sắc");
        npcSay.append("\n|2|Cần ").append(Util.numberToMoney(requiredGold)).append(" vàng");

        if (daNguSac == null || daNguSac.quantity < requiredDa) {
            long missing = requiredDa - (daNguSac == null ? 0 : daNguSac.quantity);
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    npcSay + "\n|7|Còn thiếu " + missing + " Đá ngũ sắc", "Đóng");
            return;
        }
        if (player.inventory.gold < requiredGold) {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                    npcSay + "\n|7|Còn thiếu " + Util.numberToMoney(requiredGold - player.inventory.gold) + " vàng", "Đóng");
            return;
        }

        CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                npcSay.toString(), "Làm phép", "Từ chối");
    }

    public static void startCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null || player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }

        Item costume = null;
        Item daNguSac = null;
        for (Item item : player.combine.itemsCombine) {
            if (isValidCostume(item)) {
                costume = item;
            } else if (item != null && item.isNotNullItem() && item.template != null && item.template.id == DA_NGU_SAC_ID) {
                daNguSac = item;
            }
        }

        if (costume == null || !hasExpiryDate(costume)) {
            Service.gI().sendThongBao(player, "Cần cải trang có hạn sử dụng");
            return;
        }

        int requiredDa = getRequiredDaNguSac(costume);
        long requiredGold = getRequiredGold(costume);
        if (daNguSac == null || daNguSac.quantity < requiredDa) {
            Service.gI().sendThongBao(player, "Bạn không đủ Đá ngũ sắc");
            return;
        }
        if (player.inventory.gold < requiredGold) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng");
            Service.gI().sendMoney(player);
            return;
        }

        costume.itemOptions.removeIf(Item.ItemOption::haveExpiryDate);
        player.inventory.gold -= requiredGold;
        InventoryService.gI().subQuantityItemsBag(player, daNguSac, requiredDa);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Đã vĩnh viễn cải trang " + costume.template.name + ".");
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static boolean isValidCostume(Item item) {
        return item != null && item.isNotNullItem() && item.template != null && item.template.type == 5;
    }

    private static boolean hasExpiryDate(Item item) {
        return item.itemOptions.stream().anyMatch(Item.ItemOption::haveExpiryDate);
    }

    private static int getRequiredDaNguSac(Item costume) {
        return COST_DA_NGU_SAC;
    }

    private static long getRequiredGold(Item costume) {
        return COST_GOLD;
    }
}

package models.Combine.manifest;

import consts.ConstNpc;
import item.Item;
import models.Combine.CombineService;
import nro.player.Player;
import nro.server.Manager;
import nro.services.InventoryService;
import nro.services.ItemService;
import nro.services.Service;
import utils.Util;

public class NangCapKichHoatVip {

    private static final int DA_NGU_SAC_ID = 674;
    private static final int DA_NGU_SAC_REQUIRED = 15;
    private static final int GOLD_REQUIRED = 250_000_000;

    private static final short[][][] STARTER_SHOP_ITEMS = {
            {
                    {0, 33, 3, 34, 136, 137, 138, 139, 230, 231, 232, 233},
                    {6, 35, 9, 36, 140, 141, 142, 143, 242, 243, 244, 245},
                    {21, 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257},
                    {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269},
                    {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281}
            },
            {
                    {1, 41, 4, 42, 152, 153, 154, 155, 234, 235, 236, 237},
                    {7, 43, 10, 44, 156, 157, 158, 159, 246, 247, 248, 249},
                    {22, 46, 25, 45, 160, 161, 162, 163, 258, 259, 260, 261},
                    {28, 47, 31, 48, 164, 165, 166, 167, 270, 271, 272, 273},
                    {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281}
            },
            {
                    {2, 49, 5, 50, 168, 169, 170, 171, 238, 239, 240, 241},
                    {8, 51, 11, 52, 172, 173, 174, 175, 250, 251, 252, 253},
                    {23, 53, 26, 54, 176, 177, 178, 179, 262, 263, 264, 265},
                    {29, 55, 32, 56, 180, 181, 182, 183, 274, 275, 276, 277},
                    {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281}
            }
    };

    private static final short[][] BASIC_KICH_HOAT_ITEMS = {
            {0, 6, 21, 27, 12},
            {1, 7, 22, 28, 12},
            {2, 8, 23, 29, 12}
    };

    public static boolean isDoThanLinh(Item item) {
        return item != null && item.template != null && item.isDTL()
                && item.template.type >= 0 && item.template.type <= 4;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine == null || player.combine.itemsCombine == null || player.combine.itemsCombine.size() != 2) {
            Service.gI().sendThongBaoOK(player, "Cần 1 đồ Thần Linh và 15 Đá ngũ sắc");
            return;
        }

        Item doThanLinh = null;
        Item daNguSac = null;
        for (Item item : player.combine.itemsCombine) {
            if (isDoThanLinh(item)) {
                doThanLinh = item;
            } else if (item.template.id == DA_NGU_SAC_ID) {
                daNguSac = item;
            }
        }

        if (doThanLinh == null || daNguSac == null || daNguSac.quantity < DA_NGU_SAC_REQUIRED) {
            Service.gI().sendThongBaoOK(player, "Cần 1 đồ Thần Linh và 15 Đá ngũ sắc");
            return;
        }

        player.combine.goldCombine = GOLD_REQUIRED;
        String npcSay = "Đặt 1 đồ Thần Linh, 15 Đá ngũ sắc và 250 triệu vàng\n"
                + "90% nhận ngẫu nhiên đồ trong shop cùng loại (trừ đồ Kích Hoạt farm)\n"
                + "10% nhận trang bị Kích Hoạt Thần Linh cùng loại";

        if (player.inventory.gold < GOLD_REQUIRED) {
            npcSay += "\nCòn thiếu " + Util.numberToMoney(GOLD_REQUIRED - player.inventory.gold) + " vàng";
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
        } else {
            CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                    "Nâng cấp\n" + Util.numberToMoney(GOLD_REQUIRED) + " vàng", "Từ chối");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() != 2) {
            return;
        }

        if (player.inventory.gold < GOLD_REQUIRED) {
            Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu "
                    + Util.numberToMoney(GOLD_REQUIRED - player.inventory.gold) + " vàng nữa");
            Service.gI().sendMoney(player);
            return;
        }

        Item doThanLinh = null;
        Item daNguSac = null;
        for (Item item : player.combine.itemsCombine) {
            if (isDoThanLinh(item)) {
                doThanLinh = item;
            } else if (item.template.id == DA_NGU_SAC_ID) {
                daNguSac = item;
            }
        }

        if (doThanLinh == null || daNguSac == null || daNguSac.quantity < DA_NGU_SAC_REQUIRED) {
            return;
        }

        int rewardGender = doThanLinh.template.gender >= 0 && doThanLinh.template.gender <= 2
                ? doThanLinh.template.gender : player.gender;

        int[][] selectedOptions = getActivationOptions(doThanLinh.template.gender, player.gender);

        boolean isKichHoatThanLinh = Util.isTrue(10, 100);
        Item newItem = isKichHoatThanLinh
                ? createThanLinhReward(rewardGender, doThanLinh.template.type)
                : createStarterShopReward(rewardGender, doThanLinh.template.type, doThanLinh.template.id);

        int optionRoll = Util.nextInt(selectedOptions.length);
        for (int optionId : selectedOptions[optionRoll]) {
            newItem.itemOptions.add(new Item.ItemOption(optionId, 0));
        }

        newItem.itemOptions.add(new Item.ItemOption(30, 0));

        player.inventory.gold -= GOLD_REQUIRED;
        InventoryService.gI().subQuantityItemsBag(player, doThanLinh, 1);
        InventoryService.gI().subQuantityItemsBag(player, daNguSac, DA_NGU_SAC_REQUIRED);
        InventoryService.gI().addItemBag(player, newItem);

        CombineService.gI().sendEffectSuccessCombine(player);
        Service.gI().sendThongBao(player, "Nâng cấp thành công, nhận được " + newItem.template.name
                + (isKichHoatThanLinh ? " Kích Hoạt Thần Linh!" : " bản set kích hoạt!"));

        InventoryService.gI().sendItemBag(player);
        Service.gI().sendMoney(player);
        CombineService.gI().reOpenItemCombine(player);
    }

    private static Item createThanLinhReward(int gender, int type) {
        Item newItem;
        if (type == 4) {
            newItem = ItemService.gI().createNewItem((short) 561);
        } else {
            newItem = ItemService.gI().createNewItem(Manager.trangBiKichHoatVip[gender][type]);
        }

        ItemService.gI().initBaseOptionThanLinh(newItem.template.type, newItem.itemOptions, 20, false);
        return newItem;
    }

    private static Item createStarterShopReward(int gender, int type, int excludeTempId) {
        short[] ids = STARTER_SHOP_ITEMS[gender][type];
        short randomId = ids[Util.nextInt(ids.length)];

        if (ids.length > 1) {
            while (randomId == excludeTempId || randomId == BASIC_KICH_HOAT_ITEMS[gender][type]) {
                randomId = ids[Util.nextInt(ids.length)];
            }
        }

        Item newItem = ItemService.gI().createNewItem(randomId);
        for (Item.ItemOption option : ItemService.gI().getListOptionItemShop(randomId)) {
            newItem.itemOptions.add(new Item.ItemOption(option.optionTemplate.id, option.param));
        }

        return newItem;
    }

    private static int[][] getActivationOptions(int itemGender, int playerGender) {
        int[][] tdOptions = {
                {129, 141}, // Set Sôngôku
                {127, 139}, // Set Thên Xin Hăng
                {128, 140}, // Set Kirin
                {250, 253}, // Set Kaioken
                {245, 246, 247, 248} // Set Than Vu Tru Kaio
        };

        int[][] nmOptions = {
                {132, 144},           // Set Pikkoro Daimao
                {131, 143},           // Set Ốc tiêu
                {130, 142},           // Set Picolo
                {238, 239, 240},      // Set Nail Namec
                {251, 254}            // Set Liên Hoàn
        };

        int[][] xdOptions = {
                {135, 138},           // Set Nappa
                {133, 136},           // Set Kakarot
                {134, 137},           // Set Ca Đíc
                {241, 242, 243, 244}  // Set Cadic M
        };

        if (itemGender == 0 || (itemGender == 3 && playerGender == 0)) {
            return tdOptions;
        }

        if (itemGender == 1 || (itemGender == 3 && playerGender == 1)) {
            return nmOptions;
        }

        return xdOptions;
    }
}

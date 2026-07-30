package nro.services;

import item.Item;
import item.Item.ItemOption;
import models.Template;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.player.Inventory;
import nro.player.Player;

public class CostumeCollectionService {

    private static CostumeCollectionService instance;

    public static CostumeCollectionService gI() {
        if (instance == null) {
            instance = new CostumeCollectionService();
        }
        return instance;
    }

    public boolean addEquippedCostume(Player player) {
        if (player.inventory == null || player.inventory.itemsBody.size() <= 5) {
            return false;
        }
        Item costume = player.inventory.itemsBody.get(5);
        if (costume == null || !costume.isNotNullItem() || costume.template.type != 5) {
            Service.gI().sendThongBao(player, "Cần mặc 1 cải trang trước.");
            return false;
        }
        if (costume.itemOptions.stream().anyMatch(ItemOption::haveExpiryDate)) {
            Service.gI().sendThongBao(player, "Không thể thêm cải trang có hạn sử dụng vào tủ.");
            return false;
        }
        int costumeId = costume.template.id;
        if (player.costumeCollection.contains(costumeId)) {
            Service.gI().sendThongBao(player, "Cải trang này đã có trong tủ.");
            return false;
        }
        player.costumeCollection.add(costumeId);
        Service.gI().sendThongBao(player, "Đã mở khóa ngoại hình " + costume.template.name + ".");
        return true;
    }

    public void ensureCostumeBox(Player player) {
        if (player.inventory == null || player.inventory.itemsCostumeBox == null) {
            return;
        }
        while (player.inventory.itemsCostumeBox.size() < Inventory.MAX_ITEM_COSTUME_BOX) {
            player.inventory.itemsCostumeBox.add(ItemService.gI().createItemNull());
        }
    }

    public boolean selectDisplayFromCostumeBox(Player player, int index) {
        if (player.inventory == null || index < 0 || index >= player.inventory.itemsCostumeBox.size()) {
            return false;
        }
        Item costume = player.inventory.itemsCostumeBox.get(index);
        if (costume == null || !costume.isNotNullItem() || costume.template.type != 5) {
            return false;
        }
        player.costumeDisplayId = costume.template.id;
        refreshAppearance(player);
        Service.gI().sendThongBao(player, "Đang hiển thị " + costume.template.name + ".");
        return true;
    }

    public boolean openCostumeItemMenu(Player player, Npc npc, int index) {
        if (npc == null || player.inventory == null || index < 0 || index >= player.inventory.itemsCostumeBox.size()) {
            return false;
        }
        Item costume = player.inventory.itemsCostumeBox.get(index);
        if (costume == null || !costume.isNotNullItem() || costume.template.type != 5) {
            return false;
        }
        NpcFactory.PLAYERID_OBJECT.put(player.id, index);
        npc.createOtherMenu(player, consts.ConstNpc.MENU_TU_CAI_TRANG_ITEM,
                costume.template.name, "Hiển thị", "Lấy ra");
        return true;
    }

    public boolean takeOutCostume(Player player, int index) {
        if (player.inventory == null || index < 0 || index >= player.inventory.itemsCostumeBox.size()) {
            return false;
        }
        Item costume = player.inventory.itemsCostumeBox.get(index);
        if (costume == null || !costume.isNotNullItem()) {
            return false;
        }
        String costumeName = costume.template.name;
        int costumeId = costume.template.id;
        if (!InventoryService.gI().addItemBag(player, costume)) {
            Service.gI().sendThongBao(player, "Hành trang đầy.");
            return false;
        }
        player.inventory.itemsCostumeBox.set(index, ItemService.gI().createItemNull());
        if (player.costumeDisplayId == costumeId) {
            player.costumeDisplayId = -1;
            refreshAppearance(player);
        }
        InventoryService.gI().sendItemBag(player);
        InventoryService.gI().sendItemBox(player);
        Service.gI().sendThongBao(player, "Đã lấy ra " + costumeName + ".");
        return true;
    }

    public boolean selectDisplayFromCollectionBox(Player player, int index) {
        return selectDisplayFromCostumeBox(player, index);
    }

    public boolean hasCostumeInCostumeBox(Player player, int costumeId) {
        return player.inventory != null
                && player.inventory.itemsCostumeBox != null
                && player.inventory.itemsCostumeBox.stream()
                        .anyMatch(item -> item != null && item.isNotNullItem()
                        && item.template.type == 5 && item.template.id == costumeId);
    }

    public String[] getDisplayMenus(Player player) {
        return player.costumeCollection.stream()
                .map(id -> {
                    Template.ItemTemplate template = ItemService.gI().getTemplate(id);
                    if (template == null || template.type != 5) {
                        return "Lỗi cải trang\n" + id;
                    }
                    return (player.costumeDisplayId == id ? "Đang dùng\n" : "") + template.name;
                })
                .toArray(String[]::new);
    }

    public void selectDisplay(Player player, int select) {
        if (select < 0 || select >= player.costumeCollection.size()) {
            return;
        }
        int costumeId = player.costumeCollection.get(select);
        Template.ItemTemplate template = ItemService.gI().getTemplate(costumeId);
        if (template == null || template.type != 5) {
            player.costumeCollection.remove(select);
            Service.gI().sendThongBao(player, "Cải trang này không hợp lệ.");
            return;
        }
        player.costumeDisplayId = costumeId;
        refreshAppearance(player);
        Service.gI().sendThongBao(player, "Đang hiển thị " + template.name + ".");
    }

    public void clearDisplay(Player player) {
        player.costumeDisplayId = -1;
        refreshAppearance(player);
        Service.gI().sendThongBao(player, "Đã gỡ hiển thị cải trang.");
    }

    public void refreshAppearance(Player player) {
        InventoryService.gI().sendItemBody(player);
        Service.gI().point(player);
        Service.gI().Send_Caitrang(player);
    }
}

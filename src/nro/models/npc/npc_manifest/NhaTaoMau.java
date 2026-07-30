package nro.models.npc.npc_manifest;

import consts.ConstNpc;
import nro.models.npc.Npc;
import nro.models.npc.NpcFactory;
import nro.player.Inventory;
import nro.player.Player;
import nro.services.CostumeCollectionService;
import nro.services.InventoryService;
import nro.services.Service;

public class NhaTaoMau extends Npc {

    public NhaTaoMau(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            CostumeCollectionService.gI().ensureCostumeBox(player);
            player.iDMark.setTypeBox(Inventory.TYPE_COSTUME_BOX);
            createOtherMenu(player, ConstNpc.BASE_MENU,
                    "Cất cải trang vào đây để chọn hiển thị ngoại hình.",
                    "Mở tủ", "Gỡ hiển thị", "Aura\nsao pha lê", "Hướng dẫn", "Từ chối");
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (!canOpenNpc(player)) {
            return;
        }
        if (player.iDMark.isBaseMenu()) {
            switch (select) {
                case 0 -> {
                    CostumeCollectionService.gI().ensureCostumeBox(player);
                    player.iDMark.setTypeBox(Inventory.TYPE_COSTUME_BOX);
                    InventoryService.gI().openBox(player);
                }
                case 1 -> CostumeCollectionService.gI().clearDisplay(player);
                case 2 -> openCrystalAuraMenu(player);
                case 3 -> Service.gI().sendThongBaoOK(player,
                        "Cất cải trang vào tủ này.\n"
                        + "Bấm vào cải trang trong tủ để chọn ngoại hình hiển thị.\n"
                        + "Chỉ số vẫn lấy từ cải trang đang mặc.");
            }
        } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_CHON_AURA_SAO_PHA_LE) {
            switch (select) {
                case 0 -> selectCrystalAura(player, 28, "Sói xanh");
                case 1 -> selectCrystalAura(player, 63, "Sói đỏ");
                case 2 -> {
                    player.crystalAuraId = -1;
                    CostumeCollectionService.gI().refreshAppearance(player);
                    Service.gI().sendThongBao(player, "Đã gỡ aura sao pha lê.");
                }
            }
        } else if (player.iDMark.getIndexMenu() == ConstNpc.MENU_TU_CAI_TRANG_ITEM) {
            Object object = NpcFactory.PLAYERID_OBJECT.get(player.id);
            if (!(object instanceof Integer index)) {
                return;
            }
            switch (select) {
                case 0 -> CostumeCollectionService.gI().selectDisplayFromCostumeBox(player, index);
                case 1 -> CostumeCollectionService.gI().takeOutCostume(player, index);
            }
        }
    }

    private void openCrystalAuraMenu(Player player) {
        String current = switch (player.crystalAuraId) {
            case 28 -> "Sói xanh";
            case 63 -> "Sói đỏ";
            default -> "Chưa chọn";
        };
        String status = player.hasFullCrystalSet(8)
                ? "Ngươi đã đủ quyền chọn aura sao pha lê."
                : "Cần mặc đủ áo, quần, găng, giày, rada đã ép đủ 8 sao pha le.";
        createOtherMenu(player, ConstNpc.MENU_CHON_AURA_SAO_PHA_LE,
                status + "\nAura hiện tại: " + current,
                "Sói xanh", "Sói đỏ", "Gỡ aura", "Từ chối");
    }

    private void selectCrystalAura(Player player, int auraId, String auraName) {
        if (!player.hasFullCrystalSet(8)) {
            Service.gI().sendThongBao(player, "Cần full 5 món đã ép đủ 8 sao pha lê.");
            return;
        }
        player.crystalAuraId = auraId;
        CostumeCollectionService.gI().refreshAppearance(player);
        Service.gI().sendThongBao(player, "Đã chọn sao pha lê " + auraName + ".");
    }
}

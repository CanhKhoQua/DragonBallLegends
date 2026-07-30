package nro.models.npc.npc_manifest;



import nro.models.npc.Npc;
import nro.player.Inventory;
import nro.player.Player;
import nro.services.InventoryService;

public class RuongDo extends Npc {

    public RuongDo(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.iDMark.setTypeBox(Inventory.TYPE_NORMAL_BOX);
            InventoryService.gI().openBox(player);
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {

        }
    }
}

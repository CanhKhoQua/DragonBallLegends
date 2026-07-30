package boss.boss_manifest.DeathOrAliveArena;



import boss.BossID;
import boss.BossesData;
import static boss.BossType.PHOBAN;
import nro.player.Player;

public class ThoDauBac extends DeathOrAliveArena {

    public ThoDauBac(Player player) throws Exception {
        super(PHOBAN, BossID.THO_DAU_BAC, BossesData.THO_DAU_BAC);
        this.playerAtt = player;
    }
}

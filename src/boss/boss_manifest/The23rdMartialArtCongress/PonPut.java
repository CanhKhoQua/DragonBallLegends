package boss.boss_manifest.The23rdMartialArtCongress;



import boss.BossID;
import boss.BossesData;
import static boss.BossType.PHOBAN;
import nro.player.Player;

public class PonPut extends The23rdMartialArtCongress {

    public PonPut(Player player) throws Exception {
        super(PHOBAN, BossID.PON_PUT, BossesData.PON_PUT);
        this.playerAtt = player;
    }
}

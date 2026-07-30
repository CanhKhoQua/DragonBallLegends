package event.event_manifest;



import boss.BossID;
import event.Event;

public class Christmas extends Event {

    @Override
    public void npc() {
        //chichi
        createNpc(5, 82, 231, 288);
        createNpc(14, 86, 953, 408);
        createNpc(0, 86, 965, 432);
        createNpc(7, 86, 525, 432);
    }
    
    @Override
    public void boss() {
        createBoss(BossID.ONG_GIA_NOEL, 30);
        createBoss(BossID.Virut, 10);
        createBoss(BossID.NGUYETTHAN, 10);
        createBoss(BossID.TUAN_LOC, 10);
    }
}

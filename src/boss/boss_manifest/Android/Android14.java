package boss.boss_manifest.Android;



import consts.ConstPlayer;
import boss.AppearType;
import boss.Boss;
import boss.BossID;
import boss.BossStatus;
import boss.BossesData;
import map.ItemMap;
import nro.player.Player;
import nro.services.PlayerService;
import nro.services.Service;
import nro.services.TaskService;
import utils.Util;

public class Android14 extends Boss {

    public boolean callApk13;

    public Android14() throws Exception {
        super(BossID.ANDROID_14, BossesData.ANDROID_14);
    }

    @Override
    public void reward(Player plKill) {
        int[] itemRan = new int[]{380, 381, 382, 383, 384, 385};
        int itemId = itemRan[Util.nextInt(itemRan.length)];
        if (Util.isTrue(15, 100)) {
            ItemMap it = new ItemMap(this.zone, itemId, 1, this.location.x, this.zone.map.yPhysicInTop(this.location.x,
                    this.location.y - 24), plKill.id);
            Service.gI().dropItemMap(this.zone, it);
        }
        dropThanLinhBossRewards(plKill, true);
        TaskService.gI().checkDoneTaskKillBoss(plKill, this);
    }

    @Override
    protected void resetBase() {
        super.resetBase();
        this.callApk13 = false;
    }

    @Override
    public void active() {
        if (this.typePk == ConstPlayer.NON_PK && !this.callApk13) {
            this.changeToTypePK();
        }
        this.attack();
    }

    @Override
    public synchronized long injured(Player plAtt, long damage, boolean piercing, boolean isMobAttack) {
        if (damage >= this.nPoint.hp
                && this.bossAppearTogether != null
                && this.bossAppearTogether[this.currentLevel] != null) {
            for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
                if (boss.id == BossID.ANDROID_15 && !boss.isDie()) {
                    return 0;
                }
            }
        }
        return super.injured(plAtt, damage, piercing, isMobAttack);
    }

    public void callApk13() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss.id == BossID.ANDROID_13) {
                if (boss.zone == null || boss.isDie()) {
                    boss.changeStatus(BossStatus.RESPAWN);
                } else {
                    boss.changeToTypePK();
                }
            } else if (boss.id == BossID.ANDROID_15) {
                boss.changeToTypeNonPK();
                ((Android15) boss).callApk13 = true;
                ((Android15) boss).recoverHP();
            }
        }
        this.changeToTypeNonPK();
        this.recoverHP();
        this.callApk13 = true;
    }

    public void recoverHP() {
        PlayerService.gI().hoiPhuc(this, this.nPoint.hpMax, 0);
    }

    @Override
    public void doneChatS() {
        if (this.bossAppearTogether == null
                || this.currentLevel < 0
                || this.currentLevel >= this.bossAppearTogether.length
                || this.bossAppearTogether[this.currentLevel] == null) {
            return;
        }
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            if (boss.id == BossID.ANDROID_13 || boss.id == BossID.ANDROID_15) {
                boss.changeToTypePK();
            }
        }
    }

    @Override
    protected String getNotifyName() {
        if (this.bossAppearTogether == null || this.bossAppearTogether[this.currentLevel] == null) {
            return this.name;
        }
        String name13 = null;
        String name15 = null;
        for (Boss boss : this.bossAppearTogether[this.currentLevel]) {
            int nextLevelBoss = boss.currentLevel + 1;
            if (nextLevelBoss >= boss.data.length) {
                nextLevelBoss = 0;
            }
            if (boss.data[nextLevelBoss].getTypeAppear() == AppearType.APPEAR_WITH_ANOTHER) {
                if (boss.id == BossID.ANDROID_13) {
                    name13 = boss.data[nextLevelBoss].getName();
                } else if (boss.id == BossID.ANDROID_15) {
                    name15 = boss.data[nextLevelBoss].getName();
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        if (name13 != null) {
            sb.append(name13).append(", ");
        }
        sb.append(this.name);
        if (name15 != null) {
            sb.append(", ").append(name15);
        }
        return sb.toString();
    }

}

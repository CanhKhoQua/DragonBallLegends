package boss.boss_manifest.BossTower;

import boss.BossData;
import boss.BossStatus;
import boss.BossType;
import boss.OtherBossManager;
import boss.boss_manifest.Training.TrainingBoss;
import consts.ConstPlayer;
import models.BossTower.BossTowerService;
import models.BossTower.BossTowerSession;
import network.Message;
import nro.player.Player;
import nro.services.Service;
import nro.services.SkillService;
import services.func.ChangeMapService;
import utils.Logger;
import utils.SkillUtil;
import utils.Util;

public class BossTowerBoss extends TrainingBoss {

    private final BossTowerSession session;
    private long lastMove;
    private boolean leaving;

    public BossTowerBoss(BossTowerSession session, int id, BossData data) throws Exception {
        super(BossType.PHOBAN, id, data);
        this.session = session;
        this.playerAtt = session.player;
        this.isNotifyDisabled = true;
        this.isZone01SpawnDisabled = true;
        this.bossStatus = BossStatus.RESPAWN;
    }

    @Override
    public void joinMap() {
        if (session == null || !session.isActive() || session.player == null || session.player.zone != session.zone) {
            forceLeave();
            return;
        }
        this.zone = session.zone;
        int x = 520;
        int y = this.zone.map.yPhysicInTop(x, 100);
        ChangeMapService.gI().changeMap(this, this.zone, x, y);
        Service.gI().sendFlagBag(this);
        this.changeStatus(BossStatus.ACTIVE);
    }

    @Override
    public Player getPlayerAttack() {
        if (session == null || !session.isActive()) {
            return null;
        }
        Player player = session.player;
        if (player == null || player.zone != this.zone || player.isDie()) {
            return null;
        }
        return player;
    }

    @Override
    public void active() {
        Player player = getPlayerAttack();
        if (player == null) {
            BossTowerService.gI().onPlayerFailed(session, "Đã rời Tháp Boss.");
            return;
        }
        if (this.typePk == ConstPlayer.NON_PK) {
            this.changeToTypePK();
        }
        attack();
    }

    @Override
    public void attack() {
        Player player = getPlayerAttack();
        if (player == null || this.isDie()) {
            return;
        }
        try {
            this.playerSkill.skillSelect = this.playerSkill.skills.get(Util.nextInt(0, this.playerSkill.skills.size() - 1));
            if (Util.getDistance(this, player) <= this.getRangeCanAttackWithSkillSelect()) {
                if (Util.canDoWithTime(lastMove, 1500) && Util.isTrue(25, 100)) {
                    int x = player.location.x + Util.nextInt(-80, 80);
                    int y = player.location.y;
                    if (SkillUtil.isUseSkillChuong(this)) {
                        y -= Util.nextInt(0, 50);
                    }
                    moveTo(x, y);
                    lastMove = System.currentTimeMillis();
                }
                SkillService.gI().useSkill(this, player, null, -1, null);
                checkPlayerDie(player);
            } else {
                moveToPlayer(player);
            }
        } catch (Exception e) {
            Logger.logException(BossTowerBoss.class, e);
        }
    }

    @Override
    public void checkPlayerDie(Player player) {
        if (player != null && player.isDie()) {
            BossTowerService.gI().onPlayerFailed(session, "Bạn đã thua tại tầng " + session.currentFloor + ".");
        }
    }

    @Override
    public void die(Player plKill) {
        BossTowerService.gI().onBossKilled(session, plKill, this);
        leaveMap();
    }

    @Override
    public void reward(Player plKill) {
    }

    @Override
    public void leaveMap() {
        if (leaving) {
            return;
        }
        leaving = true;
        try {
            ChangeMapService.gI().exitMap(this);
        } catch (Exception e) {
            Logger.logException(BossTowerBoss.class, e);
        }
        try {
            if (session != null && session.player != null) {
                Message msg = new Message(-6);
                msg.writer().writeInt((int) this.id);
                session.player.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception e) {
            Logger.logException(BossTowerBoss.class, e);
        }
        this.zone = null;
        this.lastZone = null;
        OtherBossManager.gI().removeBoss(this);
        this.dispose();
    }

    public void forceLeave() {
        leaveMap();
    }
}

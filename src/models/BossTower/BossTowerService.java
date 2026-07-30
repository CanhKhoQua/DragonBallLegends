package models.BossTower;

import boss.BossData;
import boss.boss_manifest.BossTower.BossTowerBoss;
import consts.ConstPlayer;
import jdbc.daos.BossTowerDAO;
import jdbc.daos.NDVSqlFetcher;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import map.Zone;
import network.Message;
import nro.player.NewPet;
import nro.player.Pet;
import nro.player.Player;
import nro.player.PlayerClone;
import nro.services.InventoryService;
import nro.services.MapService;
import nro.services.Service;
import services.func.ChangeMapService;
import skill.Skill;
import utils.Logger;
import utils.Util;

public class BossTowerService {

    private static final int MAP_ID = 178;
    private static final int MAX_FLOOR = 100;
    private static final int START_X = 180;
    private static final AtomicInteger BOSS_ID = new AtomicInteger(-900000);

    private static final int KAME_ISLAND_MAP_ID = 5;
    private static final int KAME_ISLAND_X = 1068;
    private static final int KAME_ISLAND_Y = 408;

    private static BossTowerService instance;

    private final Map<Long, BossTowerSession> sessions = new ConcurrentHashMap<>();

    private static final short[][] OUTFITS = {
        {159, 160, 161, -1, -1, -1},
        {165, 166, 167, -1, -1, -1},
        {162, 163, 164, -1, -1, -1},
        {168, 169, 170, -1, -1, -1},
        {177, 178, 179, -1, -1, -1},
        {418, 419, 420, -1, -1, -1},
        {451, 452, 453, -1, -1, -1},
        {297, 298, 299, -1, -1, -1},
        {383, 384, 385, -1, -1, -1},
        {870, 871, 872, -1, -1, -1}
    };

    private static final String[] NAMES = {
        "Kuku", "Mập Đầu Đinh", "Rambo", "Số 4", "Số 1",
        "Drabura", "Bui Bui", "Mabu", "Thần Hủy Diệt", "Whis"
    };

    public static BossTowerService gI() {
        if (instance == null) {
            instance = new BossTowerService();
        }
        return instance;
    }

    public void startChallenge(Player player, int startFloor) {
        if (player == null) {
            return;
        }
        if (sessions.containsKey(player.id)) {
            Service.gI().sendThongBao(player, "Bạn đang leo tháp boss rồi.");
            return;
        }
        Zone zone = getAvailableZone();
        if (zone == null) {
            Service.gI().sendThongBao(player, "Không tìm thấy map Tháp Boss. Kiểm tra map " + MAP_ID + " trong map_template và data/map/tile_map_data.");
            return;
        }
        int floor;
        if (startFloor <= 0) {
            BossTowerWeeklyRecord record = BossTowerDAO.getRecord((int) player.id, BossTowerDAO.currentWeekKey());
            floor = (record != null && record.maxFloor > 0) ? Math.min(MAX_FLOOR, record.maxFloor + 1) : 1;
        } else {
            floor = Math.max(1, Math.min(MAX_FLOOR, startFloor));
        }
        BossTowerSession session = new BossTowerSession(player, zone, floor);
        sessions.put(player.id, session);
        int startY = zone.map.yPhysicInTop(START_X, 100);
        ChangeMapService.gI().changeMap(player, zone, START_X, startY);
        Service.gI().sendThongBao(player, "Bắt đầu Tháp Boss tầng " + floor + ".");
        Util.setTimeout(() -> spawnFloor(session), 700);
    }

    public void stopChallenge(Player player) {
        BossTowerSession session = getSession(player);
        if (session == null) {
            Service.gI().sendThongBao(player, "Bạn không ở trong Tháp Boss.");
            return;
        }
        finishSession(session, "Đã rời Tháp Boss.", true);
    }

    public void onBossKilled(BossTowerSession session, Player killer, BossTowerBoss boss) {
        if (session == null || !session.isActive() || boss == null || session.currentBoss != boss) {
            return;
        }
        Player player = session.player;
        Player killerOwner = getOwner(killer);
        if (killerOwner != null && killerOwner.id != session.playerId) {
            Service.gI().sendThongBao(killerOwner, "Boss này thuộc phiên Tháp Boss của người khác.");
            return;
        }
        session.currentBoss = null;
        session.maxFloor = Math.max(session.maxFloor, session.currentFloor);
        BossTowerDAO.saveProgress(player, session.maxFloor, session.elapsedSeconds());
        grantPendingFloorReward(player);
        if (session.currentFloor >= MAX_FLOOR) {
            finishSession(session, "Đã vượt tầng 100. Thành tích tuần đã được lưu.", true);
            return;
        }
        int nextFloor = session.currentFloor + 1;
        int xu = BossTowerRewardService.getWeeklyXuByFloor(session.maxFloor);
        Service.gI().sendThongBao(player, "Đã vượt tầng " + session.currentFloor
                + ". Mốc thưởng tuần hiện tại: " + xu + " Xu NRO.");
        session.currentFloor = nextFloor;
        Util.setTimeout(() -> spawnFloor(session), 1500);
    }

    public void onPlayerFailed(BossTowerSession session, String reason) {
        if (session == null || !session.isActive()) {
            return;
        }
        finishSession(session, reason, true);
    }

    public void debugSetProgress(Player player, int floor) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        int clamped = Math.max(1, Math.min(MAX_FLOOR, floor));
        BossTowerDAO.saveProgress(player, clamped, 1);
        BossTowerDAO.resetFloorReward((int) player.id, BossTowerDAO.currentWeekKey());
        grantPendingFloorReward(player);
        Service.gI().sendThongBao(player, "[TEST] Đã đặt thành tích Tháp Boss tuần này thành tầng " + clamped + " và gửi thưởng mốc vào hòm thư.");
    }

    public void debugSetPreviousProgress(Player player, int floor) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        int clamped = Math.max(1, Math.min(MAX_FLOOR, floor));
        String previousWeekKey = BossTowerDAO.previousWeekKey();
        BossTowerDAO.saveProgress((int) player.id, previousWeekKey, clamped, 1);
        BossTowerDAO.resetTopReward((int) player.id, previousWeekKey);
        Service.gI().sendThongBao(player, "[TEST] Đã đặt thành tích Tháp Boss tuần trước thành tầng " + clamped + ". Dùng btower claim để test nhận top.");
    }

    public void debugResetFloorReward(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        boolean reset = BossTowerDAO.resetFloorReward((int) player.id, BossTowerDAO.currentWeekKey());
        Service.gI().sendThongBao(player, reset
                ? "[TEST] Đã reset thưởng mốc Tháp Boss tuần này. Hạ thêm boss hoặc dùng btower reward để kiểm tra."
                : "[TEST] Chưa có dữ liệu Tháp Boss tuần này để reset.");
    }

    public void debugResetTopReward(Player player) {
        if (player == null || !player.isAdmin()) {
            return;
        }
        boolean reset = BossTowerDAO.resetTopReward((int) player.id, BossTowerDAO.previousWeekKey());
        Service.gI().sendThongBao(player, reset
                ? "[TEST] Đã reset thưởng top Tháp Boss tuần trước. Dùng btower claim để nhận lại."
                : "[TEST] Chưa có dữ liệu Tháp Boss tuần trước để reset.");
    }

    public void claimWeeklyReward(Player player) {
        String previousWeekKey = BossTowerDAO.previousWeekKey();
        BossTowerWeeklyRecord previousRecord = BossTowerDAO.getRecord((int) player.id, previousWeekKey);
        if (previousRecord == null || previousRecord.maxFloor <= 0) {
            Service.gI().sendThongBao(player, "Tuần trước bạn chưa có thành tích Tháp Boss.");
            return;
        }
        if (previousRecord.topClaimed) {
            Service.gI().sendThongBao(player, "Bạn đã nhận thưởng top Tháp Boss tuần trước rồi.");
            return;
        }
        int previousRank = getRank(previousWeekKey, (int) player.id);
        int topXu = BossTowerRewardService.getWeeklyTopBonusXu(previousRank);
        int topGoldBar = BossTowerRewardService.getWeeklyTopBonusGoldBar(previousRank);
        if (topXu <= 0) {
            Service.gI().sendThongBao(player, "Bạn không nằm trong top 50 Tháp Boss tuần trước.");
            return;
        }
        if (!BossTowerRewardService.grantTopReward(player, topXu, topGoldBar)) {
            Service.gI().sendThongBao(player, "Không thể phát thưởng top Tháp Boss, vui lòng báo admin.");
            return;
        }
        BossTowerDAO.markTopClaimed((int) player.id, previousWeekKey);
        if (previousRank == 1) {
            BossTowerRewardService.grantTrumCuoiBadge(player);
        }
        Service.gI().sendThongBaoOK(player, "Đã nhận thưởng top Tháp Boss tuần trước.\nHạng " + previousRank
                + ": +" + topXu + " Xu NRO, +" + topGoldBar + " thỏi vàng.");
    }

    public void showRewardInfo(Player player) {
        Service.gI().sendThongBaoOK(player, buildRewardOverviewText(player));
    }

    public String buildRewardOverviewText(Player player) {
        String weekKey = BossTowerDAO.currentWeekKey();
        BossTowerWeeklyRecord record = BossTowerDAO.getRecord((int) player.id, weekKey);
        int maxFloor = record != null ? record.maxFloor : 0;
        int claimedFloor = record != null ? Math.min(record.claimedFloor, record.maxFloor) : 0;
        int reward = BossTowerRewardService.getWeeklyXuByFloor(maxFloor);
        int claimedReward = BossTowerRewardService.getWeeklyXuByFloor(claimedFloor);
        int canClaim = Math.max(0, reward - claimedReward);
        int goldBarReward = BossTowerRewardService.getWeeklyGoldBarByFloor(maxFloor);
        int claimedGoldBarReward = BossTowerRewardService.getWeeklyGoldBarByFloor(claimedFloor);
        int canClaimGoldBar = Math.max(0, goldBarReward - claimedGoldBarReward);
        int nextFloor = getNextRewardFloor(claimedFloor);
        int nextReward = nextFloor > 0 ? BossTowerRewardService.getWeeklyXuByFloor(nextFloor) - claimedReward : 0;
        int nextGoldBarReward = nextFloor > 0 ? BossTowerRewardService.getWeeklyGoldBarByFloor(nextFloor) - claimedGoldBarReward : 0;

        String previousWeekKey = BossTowerDAO.previousWeekKey();
        BossTowerWeeklyRecord previousRecord = BossTowerDAO.getRecord((int) player.id, previousWeekKey);
        int previousRank = previousRecord != null ? getRank(previousWeekKey, (int) player.id) : Integer.MAX_VALUE;
        int previousTopXu = previousRecord != null && !previousRecord.topClaimed
                ? BossTowerRewardService.getWeeklyTopBonusXu(previousRank)
                : 0;
        int previousTopGoldBar = previousRecord != null && !previousRecord.topClaimed
                ? BossTowerRewardService.getWeeklyTopBonusGoldBar(previousRank)
                : 0;

        StringBuilder text = new StringBuilder();
        text.append("|2|THÁP BOSS TUẦN NÀY\n");
        text.append("|7|Tầng cao nhất: ").append(maxFloor).append("\n");
        text.append("|1|Đã gửi thưởng tới mốc: tầng ").append(claimedFloor).append("\n");
        if (canClaim > 0 || canClaimGoldBar > 0) {
            text.append("|6|Còn ").append(canClaim).append(" Xu NRO + ")
                    .append(canClaimGoldBar).append(" thỏi vàng chưa gửi, hãy hạ thêm boss để nhận tự động.\n\n");
        } else if (nextFloor > 0) {
            text.append("|5|Mốc kế tiếp: tầng ").append(nextFloor).append("\n");
            text.append("|6|Leo thêm ").append(Math.max(0, nextFloor - maxFloor))
                    .append(" tầng sẽ nhận thêm ").append(nextReward).append(" Xu NRO + ")
                    .append(nextGoldBarReward).append(" thỏi vàng vào hòm thư.\n\n");
        } else {
            text.append("|6|Bạn đã chạm mốc thưởng cao nhất của tuần.\n\n");
        }
        text.append("|2|TOP TUẦN TRƯỚC: ").append(previousWeekKey).append("\n");
        if (previousRecord == null || previousRank == Integer.MAX_VALUE) {
            text.append("|1|Bạn chưa có hạng top nhận thưởng.");
        } else {
            text.append("|7|Hạng: ").append(previousRank)
                    .append(previousRecord.topClaimed ? " (đã nhận)" : "\n");
            if (!previousRecord.topClaimed) {
                text.append("|6|Thưởng top có thể nhận: ").append(previousTopXu)
                        .append(" Xu NRO + ").append(previousTopGoldBar).append(" thỏi vàng");
            }
        }
        return text.toString();
    }

    public String buildMilestoneRewardText(Player player) {
        BossTowerWeeklyRecord record = BossTowerDAO.getRecord((int) player.id, BossTowerDAO.currentWeekKey());
        int maxFloor = record != null ? record.maxFloor : 0;
        int claimedFloor = record != null ? Math.min(record.claimedFloor, record.maxFloor) : 0;
        StringBuilder text = new StringBuilder("|2|MỐC THƯỞNG THÁP BOSS\n");
        int[] floors = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int previousFloor = 0;
        for (int floor : floors) {
            int xu = BossTowerRewardService.getWeeklyXuByFloor(floor)
                    - BossTowerRewardService.getWeeklyXuByFloor(previousFloor);
            int goldBar = BossTowerRewardService.getWeeklyGoldBarByFloor(floor)
                    - BossTowerRewardService.getWeeklyGoldBarByFloor(previousFloor);
            String status;
            if (claimedFloor >= floor) {
                status = " |5|[đã gửi]";
            } else if (maxFloor >= floor) {
                status = " |6|[chờ gửi]";
            } else {
                status = "";
            }
            text.append("|1|Tầng ").append(floor).append(": ").append(xu).append(" Xu NRO + ")
                    .append(goldBar).append(" thỏi vàng").append(status).append("\n");
            previousFloor = floor;
        }
        text.append("\n|7|Đây là thưởng thêm của từng mốc, không phải tổng thưởng tới tầng đó.");
        text.append("\n|7|Nếu leo qua nhiều mốc, server tự cộng dồn các mốc chưa nhận và gửi vào hòm thư.");
        text.append("\n|7|Tầng 100 nhận thêm danh hiệu Bất Bại 7 ngày.");
        return text.toString();
    }

    public String buildPreviousTopRewardText(Player player) {
        String previousWeekKey = BossTowerDAO.previousWeekKey();
        BossTowerWeeklyRecord previousRecord = BossTowerDAO.getRecord((int) player.id, previousWeekKey);
        StringBuilder text = new StringBuilder("|2|TOP THÁP BOSS TUẦN TRƯỚC\n");
        text.append("|1|Tuần: ").append(previousWeekKey).append("\n");
        if (previousRecord == null || previousRecord.maxFloor <= 0) {
            text.append("|7|Bạn chưa có thành tích tuần trước.");
            return text.toString();
        }
        int rank = getRank(previousWeekKey, (int) player.id);
        int topXu = BossTowerRewardService.getWeeklyTopBonusXu(rank);
        int topGoldBar = BossTowerRewardService.getWeeklyTopBonusGoldBar(rank);
        text.append("|7|Tầng cao nhất: ").append(previousRecord.maxFloor).append("\n");
        text.append("|7|Hạng: ").append(rank == Integer.MAX_VALUE ? "ngoài top 50" : rank).append("\n");
        text.append("|1|Trạng thái: ").append(previousRecord.topClaimed ? "đã nhận" : "chưa nhận").append("\n");
        text.append("|6|Thưởng có thể nhận: ").append(previousRecord.topClaimed ? 0 : topXu)
                .append(" Xu NRO + ").append(previousRecord.topClaimed ? 0 : topGoldBar).append(" thỏi vàng\n\n");
        text.append("|1|Top 1: 150 Xu + 100 thỏi vàng + Trùm Cuối 7 ngày\n");
        text.append("|1|Top 2-3: 100 Xu + 50 thỏi vàng\n");
        text.append("|1|Top 4-10: 50 Xu + 25 thỏi vàng\n");
        text.append("|1|Top 11-50: 20 Xu + 10 thỏi vàng");
        return text.toString();
    }

    public String buildRewardRuleText() {
        return "|2|LUẬT THƯỞNG THÁP BOSS\n"
                + "|1|Mỗi tuần lưu tầng cao nhất và thời gian tốt nhất.\n"
                + "|1|Thưởng mốc tầng gồm Xu NRO và thỏi vàng, tự gửi vào hòm thư khi vượt mốc mới.\n"
                + "|1|Danh sách mốc hiển thị thưởng thêm của từng mốc.\n"
                + "|1|Nếu đã nhận mốc 50 rồi leo tới 80, chỉ nhận thêm thưởng mốc 60, 70 và 80.\n"
                + "|1|Top tuần này chỉ để đua hạng, chưa phát thưởng ngay.\n"
                + "|1|Sang tuần mới, Quy Lão Kame mới cho nhận thưởng top tuần trước gồm Xu NRO và thỏi vàng.\n"
                + "|7|Danh hiệu Bất Bại và Trùm Cuối không gia hạn chồng thời gian.";
    }

    private void grantPendingFloorReward(Player player) {
        if (player == null) {
            return;
        }
        String weekKey = BossTowerDAO.currentWeekKey();
        BossTowerWeeklyRecord record = BossTowerDAO.getRecord((int) player.id, weekKey);
        if (record == null || record.maxFloor <= 0) {
            return;
        }
        int reward = BossTowerRewardService.getWeeklyXuByFloor(record.maxFloor);
        int claimedReward = BossTowerRewardService.getWeeklyXuByFloor(Math.min(record.claimedFloor, record.maxFloor));
        int xu = Math.max(0, reward - claimedReward);
        int goldBarReward = BossTowerRewardService.getWeeklyGoldBarByFloor(record.maxFloor);
        int claimedGoldBarReward = BossTowerRewardService.getWeeklyGoldBarByFloor(Math.min(record.claimedFloor, record.maxFloor));
        int goldBar = Math.max(0, goldBarReward - claimedGoldBarReward);
        if (xu <= 0 && goldBar <= 0) {
            return;
        }
        if (!BossTowerRewardService.grantMilestoneRewardToMail(player, xu, goldBar)) {
            Service.gI().sendThongBao(player, "Không thể gửi thưởng mốc Tháp Boss vào hòm thư, vui lòng báo admin.");
            return;
        }
        BossTowerDAO.markFloorClaimed((int) player.id, weekKey, record.maxFloor);
        if (record.maxFloor >= MAX_FLOOR) {
            BossTowerRewardService.grantBatBaiBadge(player);
        }
        Service.gI().sendThongBao(player, "Đã gửi " + xu + " Xu NRO + " + goldBar
                + " thỏi vàng thưởng mốc Tháp Boss vào hòm thư.");
    }

    public void showTop(Player player) {
        showTop(player, BossTowerDAO.currentWeekKey(), "Top Tháp Boss tuần này");
    }

    public void showPreviousTop(Player player) {
        showTop(player, BossTowerDAO.previousWeekKey(), "Top Tháp Boss tuần trước");
    }

    private void showTop(Player player, String weekKey, String title) {
        List<BossTowerWeeklyRecord> records = BossTowerDAO.getTop(weekKey, 10);
        if (records.isEmpty()) {
            Service.gI().sendThongBao(player, "Chưa có dữ liệu " + title.toLowerCase() + ".");
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(title + " " + weekKey);
            msg.writer().writeByte(records.size());
            for (int i = 0; i < records.size(); i++) {
                BossTowerWeeklyRecord record = records.get(i);
                Player top = NDVSqlFetcher.loadById(record.playerId);
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt(record.playerId);
                msg.writer().writeShort(top != null ? top.getHead() : -1);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(top != null ? top.getBody() : -1);
                msg.writer().writeShort(top != null ? top.getLeg() : -1);
                msg.writer().writeUTF(record.playerName == null ? "Không tên" : record.playerName);
                msg.writer().writeUTF("Tầng " + record.maxFloor);
                msg.writer().writeUTF(record.bestTime + " giây");
            }
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(BossTowerService.class, e, "Lỗi hiển thị top tháp boss");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private int getNextRewardFloor(int claimedFloor) {
        int[] floors = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        for (int floor : floors) {
            if (claimedFloor < floor) {
                return floor;
            }
        }
        return -1;
    }

    private void spawnFloor(BossTowerSession session) {
        if (session == null || !session.isActive() || sessions.get(session.playerId) != session) {
            return;
        }
        Player player = session.player;
        if (player == null || player.zone != session.zone || player.isDie()) {
            finishSession(session, "Phiên Tháp Boss đã kết thúc.", true);
            return;
        }
        try {
            BossData bossData = createBossData(session.currentFloor);
            BossTowerBoss boss = new BossTowerBoss(session, nextBossId(), bossData);
            session.currentBoss = boss;
            Service.gI().sendThongBao(player, "Tầng " + session.currentFloor + ": " + bossData.getName());
        } catch (Exception e) {
            Logger.logException(BossTowerService.class, e, "Lỗi tạo boss tower");
            finishSession(session, "Không tạo được boss tầng " + session.currentFloor + ".", true);
        }
    }

    private BossData createBossData(int floor) {
        int band = Math.min(OUTFITS.length - 1, Math.max(0, (floor - 1) / 10));
        long hp = Math.min(1_800_000_000L, 5_000_000L + (long) floor * floor * 150_000L);
        long dame = Math.min(20_000_000L, 20_000L + (long) floor * floor * 250L);
        if (floor % 10 == 0) {
            hp = Math.min(1_900_000_000L, hp * 13 / 10);
            dame = dame * 12 / 10;
        }
        return new BossData(
                NAMES[band] + " [Tầng " + floor + "]",
                ConstPlayer.XAYDA,
                OUTFITS[band],
                dame,
                new long[]{hp},
                new int[]{MAP_ID},
                new int[][]{
                    {Skill.GALICK, 7, 1000},
                    {Skill.ANTOMIC, 7, 2000},
                    {Skill.KAMEJOKO, 7, 3000}
                },
                new String[]{},
                new String[]{},
                new String[]{},
                86400
        );
    }

    private Zone getAvailableZone() {
        map.Map map = MapService.gI().getMapById(MAP_ID);
        if (map == null || map.zones == null || map.zones.isEmpty()) {
            return null;
        }
        return map.zones.get(0);
    }

    private Player getOwner(Player player) {
        if (player == null) {
            return null;
        }
        if (player instanceof Pet) {
            Pet pet = (Pet) player;
            return pet.master != null ? pet.master : player;
        }
        if (player instanceof PlayerClone) {
            PlayerClone clone = (PlayerClone) player;
            return clone.master != null ? clone.master : player;
        }
        if (player instanceof NewPet) {
            NewPet newPet = (NewPet) player;
            return newPet.master != null ? newPet.master : player;
        }
        return player;
    }

    private int getRank(String weekKey, int playerId) {
        List<BossTowerWeeklyRecord> records = BossTowerDAO.getTop(weekKey, 50);
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).playerId == playerId) {
                return i + 1;
            }
        }
        return Integer.MAX_VALUE;
    }

    private BossTowerSession getSession(Player player) {
        if (player == null) {
            return null;
        }
        return sessions.get(player.id);
    }

    private int nextBossId() {
        return BOSS_ID.getAndDecrement();
    }

    private void finishSession(BossTowerSession session, String message, boolean moveHome) {
        if (session == null || !session.isActive()) {
            return;
        }
        session.close();
        sessions.remove(session.playerId);
        if (session.currentBoss != null) {
            session.currentBoss.forceLeave();
            session.currentBoss = null;
        }
        Player player = session.player;
        if (player != null) {
            boolean died = player.isDie();
            if (died) {
                Service.gI().hsChar(player, player.nPoint.hpMax, player.nPoint.mpMax);
            }
            Service.gI().sendThongBao(player, message);
            if (moveHome && player.zone == session.zone) {
                if (died) {
                    ChangeMapService.gI().changeMap(player, KAME_ISLAND_MAP_ID, -1, KAME_ISLAND_X, KAME_ISLAND_Y);
                } else {
                    ChangeMapService.gI().changeMap(player, player.gender + 21, -1, -1, 1);
                }
            }
        }
    }
}

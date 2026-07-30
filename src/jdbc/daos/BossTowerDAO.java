package jdbc.daos;

import jdbc.DBConnecter;
import models.BossTower.BossTowerWeeklyRecord;
import nro.player.Player;
import utils.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

public class BossTowerDAO {

    private static final WeekFields ISO_WEEK = WeekFields.ISO;

    private BossTowerDAO() {
    }

    public static String currentWeekKey() {
        return weekKey(LocalDate.now());
    }

    public static String previousWeekKey() {
        return weekKey(LocalDate.now().minusWeeks(1));
    }

    public static String weekKey(LocalDate date) {
        int year = date.get(ISO_WEEK.weekBasedYear());
        int week = date.get(ISO_WEEK.weekOfWeekBasedYear());
        return String.format("%04d-W%02d", year, week);
    }

    public static void saveProgress(Player player, int floor, int clearTimeSeconds) {
        if (player == null || floor <= 0) {
            return;
        }
        saveProgress((int) player.id, currentWeekKey(), floor, Math.max(clearTimeSeconds, 0));
    }

    public static void saveProgress(int playerId, String weekKey, int floor, int clearTimeSeconds) {
        String sql = "INSERT INTO boss_tower_weekly (player_id, week_key, max_floor, best_time) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "best_time = CASE "
                + "WHEN VALUES(max_floor) > max_floor THEN VALUES(best_time) "
                + "WHEN VALUES(max_floor) = max_floor AND (best_time = 0 OR VALUES(best_time) < best_time) THEN VALUES(best_time) "
                + "ELSE best_time END, "
                + "claimed = CASE WHEN VALUES(max_floor) > max_floor THEN 0 ELSE claimed END, "
                + "max_floor = GREATEST(max_floor, VALUES(max_floor))";
        try {
            DBConnecter.executeUpdate(sql, playerId, weekKey, floor, Math.max(clearTimeSeconds, 0));
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi luu thanh tich thap boss");
        }
    }

    public static BossTowerWeeklyRecord getRecord(int playerId, String weekKey) {
        String sql = "SELECT btw.player_id, p.name, btw.week_key, btw.max_floor, btw.best_time, btw.claimed, btw.claimed_floor, btw.top_claimed "
                + "FROM boss_tower_weekly btw "
                + "LEFT JOIN player p ON p.id = btw.player_id "
                + "WHERE btw.player_id = ? AND btw.week_key = ? LIMIT 1";
        try (Connection con = DBConnecter.getConnectionServer();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            ps.setString(2, weekKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return readRecord(rs);
                }
            }
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi lay thanh tich thap boss");
        }
        return null;
    }

    public static List<BossTowerWeeklyRecord> getTop(String weekKey, int limit) {
        List<BossTowerWeeklyRecord> records = new ArrayList<>();
        String sql = "SELECT btw.player_id, p.name, btw.week_key, btw.max_floor, btw.best_time, btw.claimed, btw.claimed_floor, btw.top_claimed "
                + "FROM boss_tower_weekly btw "
                + "INNER JOIN player p ON p.id = btw.player_id "
                + "INNER JOIN account a ON a.id = p.account_id "
                + "WHERE btw.week_key = ? AND a.ban = 0 AND btw.max_floor > 0 "
                + "ORDER BY btw.max_floor DESC, btw.best_time ASC, btw.updated_at ASC "
                + "LIMIT ?";
        try (Connection con = DBConnecter.getConnectionServer();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, weekKey);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(readRecord(rs));
                }
            }
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi lay top thap boss");
        }
        return records;
    }

    public static boolean isClaimed(int playerId, String weekKey) {
        BossTowerWeeklyRecord record = getRecord(playerId, weekKey);
        return record != null && record.claimed;
    }

    public static boolean markClaimed(int playerId, String weekKey) {
        String sql = "UPDATE boss_tower_weekly SET claimed = 1 WHERE player_id = ? AND week_key = ? AND claimed = 0";
        try {
            return DBConnecter.executeUpdate(sql, playerId, weekKey) > 0;
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi danh dau da nhan thuong thap boss");
        }
        return false;
    }

    public static boolean markFloorClaimed(int playerId, String weekKey, int claimedFloor) {
        String sql = "UPDATE boss_tower_weekly "
                + "SET claimed_floor = GREATEST(claimed_floor, ?), "
                + "claimed = CASE WHEN max_floor <= GREATEST(claimed_floor, ?) THEN 1 ELSE 0 END "
                + "WHERE player_id = ? AND week_key = ?";
        try {
            return DBConnecter.executeUpdate(sql, claimedFloor, claimedFloor, playerId, weekKey) > 0;
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi danh dau moc thuong thap boss");
        }
        return false;
    }

    public static boolean markTopClaimed(int playerId, String weekKey) {
        String sql = "UPDATE boss_tower_weekly SET top_claimed = 1 WHERE player_id = ? AND week_key = ? AND top_claimed = 0";
        try {
            return DBConnecter.executeUpdate(sql, playerId, weekKey) > 0;
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi danh dau thuong top thap boss");
        }
        return false;
    }

    public static boolean resetFloorReward(int playerId, String weekKey) {
        String sql = "UPDATE boss_tower_weekly SET claimed_floor = 0, claimed = 0 WHERE player_id = ? AND week_key = ?";
        try {
            return DBConnecter.executeUpdate(sql, playerId, weekKey) > 0;
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi reset thuong moc thap boss");
        }
        return false;
    }

    public static boolean resetTopReward(int playerId, String weekKey) {
        String sql = "UPDATE boss_tower_weekly SET top_claimed = 0 WHERE player_id = ? AND week_key = ?";
        try {
            return DBConnecter.executeUpdate(sql, playerId, weekKey) > 0;
        } catch (Exception e) {
            Logger.logException(BossTowerDAO.class, e, "Loi reset thuong top thap boss");
        }
        return false;
    }

    private static BossTowerWeeklyRecord readRecord(ResultSet rs) throws Exception {
        return new BossTowerWeeklyRecord(
                rs.getInt("player_id"),
                rs.getString("name"),
                rs.getString("week_key"),
                rs.getInt("max_floor"),
                rs.getInt("best_time"),
                rs.getBoolean("claimed"),
                rs.getInt("claimed_floor"),
                rs.getBoolean("top_claimed")
        );
    }
}

package zalo.services;

import zalo.server.Settings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NroAccountService {
    

    
    private static NroAccountService instance;
    
    private NroAccountService() {
    }
    
    public static NroAccountService gI() {
        if (instance == null) {
            instance = new NroAccountService();
        }
        return instance;
    }
    
    private Connection getConnection() throws SQLException {
        String dbUrl = Settings.getDatabaseUrlA();
        String dbUser = Settings.getDatabaseUsername();
        String dbPassword = Settings.getDatabasePassword();
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
    
    public boolean updateAccountVnd(String username, int amount) {
        try (Connection conn = getConnection()) {
            String updateSql;
            if ("all".equalsIgnoreCase(username)) {
                updateSql = "UPDATE account SET vnd = vnd + ?, danap = danap + ?";
            } else {
                updateSql = "UPDATE account SET vnd = vnd + ?, danap = danap + ? WHERE username = ?";
            }
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, amount);
                pstmt.setInt(2, amount);
                if (!"all".equalsIgnoreCase(username)) {
                    pstmt.setString(3, username);
                }
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
        }
        return false;
    }
    
    public boolean setAccountBanned(String username, boolean banned) {
        try (Connection conn = getConnection()) {
            String updateSql = "UPDATE account SET ban = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, banned ? 1 : 0);
                pstmt.setString(2, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
        }
        return false;
    }
    
    public boolean setAccountAdmin(String username, boolean admin) {
        try (Connection conn = getConnection()) {
            String updateSql = "UPDATE account SET is_admin = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, admin ? 1 : 0);
                pstmt.setString(2, username);
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
        }
        return false;
    }
    
    public boolean addMemberToVip(String username) {
        try (Connection conn = getConnection()) {
            String updateSql;
            if ("all".equalsIgnoreCase(username)) {
                updateSql = "UPDATE account SET vip = vip + 1";
            } else {
                updateSql = "UPDATE account SET vip = vip + 1 WHERE username = ?";
            }
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                if (!"all".equalsIgnoreCase(username)) {
                    pstmt.setString(1, username);
                }
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
        }
        return false;
    }
}


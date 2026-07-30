package nro.server;


import utils.Functions;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import map.ItemMap;
import nro.player.Player;
import network.SessionManager;
import network.inetwork.ISession;
import nro.server.io.MySession;
import nro.services.Service;
import services.func.ChangeMapService;
import services.func.SummonDragon;
import services.func.TransactionService;
import nro.services.NgocRongNamecService;
import utils.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import models.DragonNamecWar.TranhNgoc;
import network.Session;
import services.func.SummonDragonNamek;
import utils.Util;

public class Client implements Runnable {

    private static Client instance;

    @Getter
    private final Map<Long, Player> players_id = new HashMap<>();
    private final Map<Integer, Player> players_userId = new HashMap<>();
    private final Map<String, Player> players_name = new HashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Session> sessions = new ArrayList<>();
    private final Map<Integer, MySession> sessionsByUserId = new ConcurrentHashMap<>();
    private final Map<String, Set<MySession>> sessionsByIp = new ConcurrentHashMap<>();
    private volatile boolean adminMode;

    private Client() {
        Thread.startVirtualThread(() -> run());
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public static Client gI() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void put(Player player) {
        if (!players_id.containsKey(player.id)) {
            this.players_id.put(player.id, player);
        }
        if (!players_name.containsValue(player)) {
            this.players_name.put(player.name, player);
        }
        if (!players_userId.containsValue(player)) {
            this.players_userId.put(player.getSession().userId, player);
        }
        if (!players.contains(player)) {
            this.players.add(player);
        }

    }

    public void cleanupSession(MySession session) {
        if (session == null) {
            return;
        }
        if (session.cleanupFinished) {
            removeSessionFromManager(session);
            return;
        }
        session.cleanupFinished = true;
        Player player = session.player;
        if (player != null) {
            MySession playerSession = player.getSession();
            session.player = null;
            player.setSession(null);
            this.remove(player, playerSession);
            player.dispose();
            if (playerSession != null) {
                this.unregisterSession(playerSession);
            }
        }
        this.unregisterSession(session);
        if (session.joinedGame) {
            session.joinedGame = false;
            if (session.userId > 0) {
                try {
                    DBConnecter.executeUpdate("update account set last_time_logout = ? where id = ?", new Timestamp(System.currentTimeMillis()), session.userId);
                } catch (Exception e) {
                    Logger.logException(Client.class, e);
                }
            }
        }
        session.logging = false;
        session.loginSuccess = false;
        session.actived = false;
        session.joinedGame = false;
        ServerManager.gI().disconnect(session);
        removeSessionFromManager(session);
    }

    private void remove(Player player, MySession playerSession) {
        this.players_id.remove(player.id);
        this.players_name.remove(player.name);
        if (playerSession != null && playerSession.userId > 0) {
            this.players_userId.remove(playerSession.userId);
        }
        this.players.remove(player);
        if (!player.beforeDispose) {
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;
                TranhNgoc.gI().removePlayersBlue(player);
            TranhNgoc.gI().removePlayersRed(player);
            if (player.idNRNM != -1) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1, player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);
                NgocRongNamecService.gI().pNrNamec[player.idNRNM - 353] = "";
                NgocRongNamecService.gI().idpNrNamec[player.idNRNM - 353] = -1;
                player.idNRNM = -1;
            }
            ChangeMapService.gI().exitMap(player);
            TransactionService.gI().cancelTrade(player);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }
            if (SummonDragonNamek.gI().playerSummonShenron != null
                    && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                SummonDragonNamek.gI().isPlayerDisconnect = true;
            }
            if (player.shenronEvent != null) {
                player.shenronEvent.isPlayerDisconnect = true;
            }
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.pet);
            }
        }
        PlayerDAO.updatePlayer(player);
    }

    public MySession getSessionByUser(int userId) {
        return this.sessionsByUserId.get(userId);
    }

    public void registerSession(MySession session) {
        if (session == null || session.userId <= 0) {
            return;
        }
        this.sessionsByUserId.put(session.userId, session);
        this.sessionsByIp.computeIfAbsent(session.ipAddress, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregisterSession(MySession session) {
        if (session == null) {
            return;
        }
        if (session.userId > 0) {
            this.sessionsByUserId.remove(session.userId, session);
        }
        if (session.ipAddress != null) {
            Set<MySession> sessionSet = this.sessionsByIp.get(session.ipAddress);
            if (sessionSet != null) {
                sessionSet.remove(session);
                if (sessionSet.isEmpty()) {
                    this.sessionsByIp.remove(session.ipAddress, sessionSet);
                }
            }
        }
    }

    public void kickSession(MySession session) {
        if (session == null) {
            return;
        }
        if (session.isDisposed()) {
            this.cleanupSession(session);
            return;
        }
        this.cleanupSession(session);
        session.disconnect();
    }

    public boolean isAdminMode() {
        return adminMode;
    }

    public void setAdminMode(boolean adminMode) {
        this.adminMode = adminMode;
    }

    public int kickNonAdminPlayersForAdminMode() {
        int kicked = 0;
        List<Player> snapshot = new ArrayList<>(players);
        for (Player player : snapshot) {
            if (player == null || player.getSession() == null) {
                continue;
            }
            MySession session = (MySession) player.getSession();
            if (!session.isAdmin) {
                kicked++;
                kickSession(session);
            }
        }
        return kicked;
    }

    public Player getPlayer(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getRandPlayer() {
        if (this.players.isEmpty()) {
            return null;
        }
        return this.players.get(Util.nextInt(players.size()));
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Player getPlayer(String name) {
        return this.players_name.get(name);
    }

    public Player getPlayerByID(int playerId) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player != null && player.id == playerId) {
                return player;
            }
        }
        return null;
    }

    public void close() {
        Logger.log(Logger.YELLOW, "BEGIN KICK OUT SESSION " + players.size() + "\n");
        while (!players.isEmpty()) {
            Player pl = players.remove(0);
            if (pl != null && pl.getSession() != null) {
                this.kickSession((MySession) pl.getSession());
            }
        }
        Logger.log("SUCCESSFUL\n");
    }

    private void update() {
        for (int i = SessionManager.gI().getSessions().size() - 1; i >= 0; i--) {
            ISession s = SessionManager.gI().getSessions().get(i);
            MySession session = (MySession) s;
            if (session == null) {
                SessionManager.gI().getSessions().remove(i);
                continue;
            }
            if (session.isDisposed() || !session.isConnected()) {
                cleanupSession(session);
                SessionManager.gI().getSessions().remove(i);
                continue;
            }
            if (session.timeWait > 0) {
                session.timeWait--;
                if (session.timeWait == 0) {
                    kickSession(session);
                    removeSessionFromManager(session);
                    continue;
                }
            }
            if (System.currentTimeMillis() - session.lastTimeReadMessage > 120_000L) {
                if (session.player == null && !session.loginSuccess) {
                    kickSession(session);
                    removeSessionFromManager(session);
                } else if (session.player != null && !session.actived) {
                    kickSession(session);
                    removeSessionFromManager(session);
                }
            }
        }
    }

    private void removeSessionFromManager(MySession session) {
        try {
            if (session != null && SessionManager.gI() != null && SessionManager.gI().getSessions() != null) {
                SessionManager.gI().getSessions().remove(session);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - st), 10));
        }
    }

    public void show(Player player) {
        String txt = "";
        txt += "sessions: " + SessionManager.gI().getNumSession() + "\n";
        txt += "players_id: " + players_id.size() + "\n";
        txt += "players_userId: " + players_userId.size() + "\n";
        txt += "players_name: " + players_name.size() + "\n";
        txt += "players: " + players.size() + "\n";
        Service.gI().sendThongBao(player, txt);
    }
}

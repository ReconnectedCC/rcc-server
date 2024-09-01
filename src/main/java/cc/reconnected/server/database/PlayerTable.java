package cc.reconnected.server.database;

import cc.reconnected.server.RccServer;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerTable {
    private final HashMap<UUID, PlayerData> cache = new HashMap<>();

    private DatabaseClient database() {
        return RccServer.getInstance().database();
    }

    public void ensureDatabaseCreated() {
        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "uuid UUID NOT NULL PRIMARY KEY," +
                            "firstJoined TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "lastKnownName VARCHAR(16)," +
                            "discordId VARCHAR," +
                            "isBot BOOL DEFAULT FALSE," +
                            "isAlt BOOL DEFAULT FALSE," +
                            "pronouns VARCHAR DEFAULT NULL" +
                            ");");

            stmt.executeUpdate();
            stmt.close();

        } catch (SQLException e) {
            RccServer.LOGGER.error("Could not create players data tables", e);
        }
    }

    public void refreshPlayerData(UUID uuid) {
        cache.remove(uuid);
    }

    public void clearCache() {
        cache.clear();
    }

    public boolean exists(UUID uuid) {
        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("SELECT uuid FROM players WHERE uuid = ?;");
            stmt.setObject(1, uuid);
            var set = stmt.executeQuery();
            var exists = set.next();
            stmt.close();

            return exists;
        } catch (SQLException e) {
            RccServer.LOGGER.error("Could not get player data from database", e);
            return false;
        }
    }

    @Nullable
    public PlayerData getPlayerData(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }

        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?;");
            stmt.setObject(1, uuid);
            var set = stmt.executeQuery();
            if (!set.next()) {
                return null;
            }

            var playerData = new PlayerData(set.getObject("uuid", UUID.class));
            var firstJoinTimestamp = set.getObject("firstJoined", Timestamp.class);
            playerData.firstJoinedDate(new Date(firstJoinTimestamp.getTime()));
            playerData.name(set.getString("lastKnownName"));
            playerData.discordId(set.getString("discordId"));
            playerData.isBot(set.getBoolean("isBot"));
            playerData.isAlt(set.getBoolean("isAlt"));
            playerData.pronouns(set.getString("pronouns"));

            stmt.close();

            cache.put(uuid, playerData);
            return playerData;
        } catch (SQLException e) {
            RccServer.LOGGER.error("Could not get player data from database", e);
            return null;
        }
    }

    public boolean deletePlayerData(UUID uuid) {
        cache.remove(uuid);
        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("DELETE FROM players WHERE uuid = ?;");
            stmt.setObject(1, uuid);
            stmt.execute();
            stmt.close();

            return true;
        } catch(SQLException e) {
            RccServer.LOGGER.error("Could not delete player data from database", e);
            return false;
        }
    }

    public boolean createPlayerData(PlayerData playerData) {
        if(exists(playerData.uuid())) {
            return updatePlayerData(playerData);
        }

        cache.put(playerData.uuid(), playerData);

        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("INSERT INTO players(uuid, firstJoined, lastKnownName, discordId, isBot, isAlt, pronouns) VALUES (?,?,?,?,?,?,?);");
            stmt.setObject(1, playerData.uuid());
            var timestamp = new Timestamp(playerData.firstJoinedDate().getTime());
            stmt.setTimestamp(2, timestamp);
            stmt.setString(3, playerData.name());
            stmt.setString(4, playerData.discordId());
            stmt.setBoolean(5, playerData.isBot());
            stmt.setBoolean(6, playerData.isAlt());
            stmt.setString(7, playerData.pronouns());
            stmt.execute();
            stmt.close();

            return true;
        } catch(SQLException e) {
            RccServer.LOGGER.error("Could not create player data from database", e);
            return false;
        }
    }

    public boolean updatePlayerData(PlayerData playerData) {
        if(!exists(playerData.uuid())) {
            return createPlayerData(playerData);
        }

        cache.put(playerData.uuid(), playerData);

        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("UPDATE players SET lastknownname = ?, discordid = ?, isBot = ?, isAlt = ?, pronouns = ? WHERE uuid = ?");
            //var stmt = conn.prepareStatement("INSERT INTO players(uuid, firstJoined, lastKnownName, discordId, isBot, isAlt, pronouns) VALUES (?,?,?,?,?,?,?);");
            stmt.setString(1, playerData.name());
            stmt.setString(2, playerData.discordId());
            stmt.setBoolean(3, playerData.isBot());
            stmt.setBoolean(4, playerData.isAlt());
            stmt.setString(5, playerData.pronouns());
            stmt.setObject(6, playerData.uuid());
            stmt.execute();
            stmt.close();

            return true;
        } catch (SQLException e) {
            RccServer.LOGGER.error("Could not update player data on database", e);
            return false;
        }
    }
}

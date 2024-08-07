package ct.server.database;

import ct.server.CtServer;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class PlayerTable {
    private final HashMap<UUID, PlayerData> cache = new HashMap<>();

    private DatabaseClient database() {
        return CtServer.getInstance().database();
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
            CtServer.LOGGER.error("Could not create players data tables", e);
        }
    }

    public void refreshPlayerData(UUID uuid) {
        cache.remove(uuid);
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
            CtServer.LOGGER.error("Could not get player data from database", e);
            return null;
        }
    }

    public boolean deletePlayerData(UUID uuid) {
        try {
            var conn = database().connection();

            var stmt = conn.prepareStatement("DELETE FROM players WHERE uuid = ?;");
            stmt.setObject(1, uuid);
            stmt.execute();
            stmt.close();

            cache.remove(uuid);
            return true;
        } catch(SQLException e) {
            CtServer.LOGGER.error("Could not delete player data from database", e);
            return false;
        }
    }

    public boolean updatePlayerData(PlayerData playerData) {
        deletePlayerData(playerData.uuid());
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

            cache.put(playerData.uuid(), playerData);
            return true;
        } catch (SQLException e) {
            CtServer.LOGGER.error("Could not get player data from database", e);
            return false;
        }
    }
}

package ct.server.database;

import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    @Nullable
    private String name;
    private Date firstJoinedDate;
    @Nullable
    private String discordId;

    private boolean isBot = false;
    private boolean isAlt = false;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }


    public String name() {
        if (name == null) {
            return uuid.toString();
        }
        return name;
    }
    public void name(@Nullable String name) {
        this.name = name;
    }

    public Date firstJoinedDate() {
        return firstJoinedDate;
    }
    public void firstJoinedDate(Date firstJoinedDate) {
        this.firstJoinedDate = firstJoinedDate;
    }

    public @Nullable String discordId() {
        return discordId;
    }
    public void discordId(@Nullable String discordId) {
        this.discordId = discordId;
    }

    public boolean isBot() {
        return isBot;
    }
    public void isBot(boolean isBot) {
        this.isBot = isBot;
    }

    public boolean isAlt() {
        return isAlt;
    }
    public void isAlt(boolean isAlt) {
        this.isAlt = isAlt;
    }
}

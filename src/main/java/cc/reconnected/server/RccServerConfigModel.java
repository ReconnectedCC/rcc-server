package cc.reconnected.server;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;

import java.util.*;

@Config(name = "rcc-server-config", wrapperName = "RccServerConfig")
public class RccServerConfigModel {
    @Nest
    public HttpApi httpApi = new HttpApi();

    @Nest
    public Afk afk = new Afk();

    @Nest
    public DirectMessages directMessages = new DirectMessages();

    @Nest
    public TeleportRequests teleportRequests = new TeleportRequests();

    @Nest
    public Homes homes = new Homes();

    @Nest
    public CustomTabList customTabList = new CustomTabList();

    @Nest
    public NearCommand nearCommand = new NearCommand();

    @Nest
    public AutoRestart autoRestart = new AutoRestart();

    @Nest
    public CustomNameConfig customName = new CustomNameConfig();

    public static class HttpApi {
        public boolean enableHttpApi = true;
        public int httpPort = 25581;
    }

    public static class Afk {
        public int afkTimeTrigger = 300;
        public String afkMessage = "<gray><displayname> is now AFK</gray>";
        public String afkReturnMessage = "<gray><displayname> is no longer AFK</gray>";
        public String afkTag = "<gray>[AFK]</gray> ";
    }

    public static class DirectMessages {
        public String tellMessage = "<gold>[</gold><source> <gray>→</gray> <target><gold>]</gold> <message>";
        public String tellMessageSpy = "\uD83D\uDC41 <gray>[<source> → <target>] <message></gray>";
    }

    public static class TeleportRequests {
        public int teleportRequestTimeout = 120;
    }

    public static class Homes {
        public int maxHomes = -1;
    }

    public static class CustomTabList {
        public boolean enableTabList = true;
        public int tabListTickDelay = 5;
        public double tabPhasePeriod = 300;
        public ArrayList<String> tabHeader = new ArrayList<>(List.of(
                "<gradient:#DEDE6C:#CC4C4C:{phase}><st>                                  </st></gradient>"
        ));

        public ArrayList<String> tabFooter = new ArrayList<>(List.of(
                "<gradient:#DEDE6C:#CC4C4C:{phase}><st>                                  </st></gradient>"
        ));

        public String playerTabName = "%rcc-server:afk%%player:displayname_visual%";
    }

    public static class NearCommand {
        public int nearCommandMaxRange = 48;
        public int nearCommandDefaultRange = 32;
    }

    public static class AutoRestart {
        public boolean enableAutoRestart = true;
        public String restartBarLabel = "Server restarting in <remaining_time>";
        public String restartKickMessage = "The server is restarting!";
        public String restartChatMessage = "<red>The server is restarting in </red><gold><remaining_time></gold>";

        public ArrayList<String> restartAt = new ArrayList<>(List.of(
                "06:00",
                "18:00"
        ));

        public String restartSound = "minecraft:block.note_block.bell";
        public float restartSoundPitch = 0.9f;

        public ArrayList<Integer> restartNotifications = new ArrayList<>(List.of(
                600,
                300,
                120,
                60,
                30,
                15,
                10,
                5,
                4,
                3,
                2,
                1
        ));
    }

    public static class CustomNameConfig {
        public LinkedHashMap<String, String> formats = new LinkedHashMap<>(Map.of(
                "admin", "<red><username></red>",
                "default", "<green><username></green>"
        ));
    }
}

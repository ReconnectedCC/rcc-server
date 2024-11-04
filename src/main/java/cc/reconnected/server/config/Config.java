package cc.reconnected.server.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    Config() {
    }

    public HttpApi httpApi = new HttpApi();
    public Afk afk = new Afk();
    public DirectMessages directMessages = new DirectMessages();
    public TeleportRequests teleportRequests = new TeleportRequests();
    public Homes homes = new Homes();
    public CustomTabList customTabList = new CustomTabList();
    public NearCommand nearCommand = new NearCommand();
    public AutoRestart autoRestart = new AutoRestart();
    public TextFormats textFormats = new TextFormats();

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

    public static class TextFormats {
        public record NameFormat(String group, String format) {
        }

        public boolean enableChatMarkdown = true;

        public ArrayList<NameFormat> nameFormats = new ArrayList<>(List.of(
                new NameFormat("admin", "<red>%player:name%</red>"),
                new NameFormat("default", "<green>%player:name%</green>")
        ));

        public String chatFormat = "${player}<gray>:</gray> ${message}";
        public String emoteFormat = "<gray>\uD83D\uDC64 ${player} <i>${message}</i></gray>";
        public String joinFormat = "<green>+</green> ${player} <yellow>joined!</yellow>";
        public String joinRenamedFormat = "<green>+</green> ${player} <yellow>joined! <i>(Previously known as ${previousName})</i></yellow>";
        public String leaveFormat = "<red>-</red> ${player} <yellow>left!</yellow>";
        public String deathFormat = "<gray>\u2620 ${message}</gray>";
    }


}

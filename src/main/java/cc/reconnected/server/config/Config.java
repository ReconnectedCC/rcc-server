package cc.reconnected.server.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    Config() {
    }

    public HttpApi httpApi = new HttpApi();
    public Afk afk = new Afk();
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

        public String chatFormat = "%player:displayname%<gray>:</gray> ${message}";
        public String emoteFormat = "<gray>\uD83D\uDC64 %player:displayname% <i>${message}</i></gray>";
        public String joinFormat = "<green>+</green> %player:displayname% <yellow>joined!</yellow>";
        public String joinRenamedFormat = "<green>+</green> %player:displayname% <yellow>joined! <i>(Previously known as ${previousName})</i></yellow>";
        public String leaveFormat = "<red>-</red> %player:displayname% <yellow>left!</yellow>";
        public String deathFormat = "<gray>\u2620 ${message}</gray>";

        public Commands commands = new Commands();

        public static class Commands {
            public Common common = new Common();
            public Home home = new Home();
            public Spawn spawn = new Spawn();
            public TeleportRequest teleportRequest = new TeleportRequest();
            public Tell tell = new Tell();
            public Warp warp = new Warp();
            public Afk afk = new Afk();

            public static class Common {
                // `{{command}}` is replaced as a string before parsing
                public String button = "<click:run_command:'{{command}}'><hover:show_text:'${hoverText}'><aqua>[</aqua>${label}<aqua>]</aqua></hover></click>";
                public String accept = "<green>Accept</green>";
                public String refuse = "<red>Refuse</red>";
            }

            public static class Home {
                public String teleporting = "<gold>Teleporting to <yellow>${home}</yellow></gold>";
                public String homeExists = "<gold>You already have set this home.</gold>\n ${forceSetButton}";
                public String homeNotFound = "<red>The home <yellow>${home}</yellow> does not exist!</red>";
                public String maxHomesReached = "<red>You have reached the maximum amount of homes!</red>";
                public String homeSetSuccess = "<gold>New home <yellow>${home}</yellow> set!</gold>";
            }

            public static class Spawn {
                public String teleporting = "<gold>Teleporting to spawn...</gold>";
            }

            public static class TeleportRequest {
                public String playerNotFound = "<red>Player <yellow>${targetPlayer}</yellow> not found!</red>";
                public String requestSent = "<gold>Teleport request sent.</gold>";
                public String pendingTeleport = "${requesterPlayer} <gold>requested to teleport to you.</gold>\n ${acceptButton} ${refuseButton}";
                public String pendingTeleportHere = "${requesterPlayer} <gold>requested you to teleport to them.</gold>\n ${acceptButton} ${refuseButton}";
                public String hoverAccept = "Click to accept request";
                public String hoverRefuse = "Click to refuse request";
            }

            public static class Tell {
                public String playerNotFound = "<red>Player <yellow>${targetPlayer}</yellow> not found!</red>";
                public String you = "<gray><i>You</i></gray>";
                public String message = "<gold>[</gold>${sourcePlayer} <gray>→</gray> ${targetPlayer}<gold>]</gold> ${message}";
                public String messageSpy = "\uD83D\uDC41 <gray>[${sourcePlayer} → ${targetPlayer}] ${message}</gray>";
                public String noLastSenderReply = "<red>You have no one to reply to.</red>"; // relatable
            }

            public static class Warp {
                public String teleporting = "<gold>Warping to <yellow>${warp}</yellow>...</gold>";
                public String warpNotFound = "<red>The warp <yellow>${warp}</yellow> does not exist!</red>";
            }

            public static class Afk {
                public String goneAfk = "<gray>%player:displayname% is now AFK</gray>";
                public String returnAfk = "<gray>%player:displayname% is no longer AFK</gray>";
                public String tag = "<gray>[AFK]</gray> ";
            }
        }
    }


}

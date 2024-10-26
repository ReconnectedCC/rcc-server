package cc.reconnected.server;

import io.wispforest.owo.config.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@Config(name = "rcc-server-config", wrapperName = "RccServerConfig")
public class RccServerConfigModel {
    public boolean enableHttpApi = true;
    public int httpPort = 25581;

    public int afkTimeTrigger = 300;

    public String afkMessage = "<gray><displayname> is now AFK</gray>";
    public String afkReturnMessage = "<gray><displayname> is no longer AFK</gray>";
    public String afkTag = "<gray>[AFK]</gray> ";

    public String tellMessage = "<gold>[</gold><source> <gray>→</gray> <target><gold>]</gold> <message>";
    public String tellMessageSpy = "\uD83D\uDC41 <gray>[<source> → <target>]</gray> <message>";

    public int teleportRequestTimeout = 120;

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

    public int nearCommandMaxRange = 48;
    public int nearCommandDefaultRange = 32;

    public boolean enableAutoRestart = true;
    public ArrayList<String> restartAt = new ArrayList<>(List.of(
            "06:00",
            "18:00"
    ));
}

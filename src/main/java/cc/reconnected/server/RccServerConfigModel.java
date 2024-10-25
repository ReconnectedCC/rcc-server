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

    public String tellMessage = "<gold>[</gold><source> <gray>→</gray> <target><gold>]</gold> <message>";
    public String tellMessageSpy = "\uD83D\uDC41 <gray>[<source> → <target>]</gray> <message>";

    public int teleportRequestTimeout = 120;

    public boolean enableTabList = true;
    public ArrayList<String> tabHeader = new ArrayList<>(List.of(
            "<gradient:#DEDE6C:#CC4C4C:{phase}><st>                                  </st></gradient>"
    ));

    public ArrayList<String> tabFooter = new ArrayList<>(List.of(
            "<gradient:#DEDE6C:#CC4C4C:{phase}><st>                                  </st></gradient>"
    ));
}

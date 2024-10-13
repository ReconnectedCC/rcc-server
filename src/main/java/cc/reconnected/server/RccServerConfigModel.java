package cc.reconnected.server;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "rcc-server-config", wrapperName = "RccServerConfig")
public class RccServerConfigModel {
    public boolean enableHttpApi = true;
    public int httpPort = 25581;

    public int afkTimeTrigger = 300;
}

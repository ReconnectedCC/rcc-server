package cc.reconnected.server;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "rcc-server-config", wrapperName = "RccServerConfig")
public class RccServerConfigModel {
    public short httpPort = 25581;
}

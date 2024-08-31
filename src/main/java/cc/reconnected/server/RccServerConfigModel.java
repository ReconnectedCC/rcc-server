package cc.reconnected.server;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "rcc-server-config", wrapperName = "RccServerConfig")
public class RccServerConfigModel {
    public short httpPort = 25581;
    public String databaseUrl = "jdbc:postgresql://127.0.0.1:5432/rcc?user=myuser&password=mypassword";
}

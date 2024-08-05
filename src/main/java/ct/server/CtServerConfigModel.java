package ct.server;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "ct-server-config", wrapperName = "CtServerConfig")
public class CtServerConfigModel {
    public short httpPort = 25581;
    public String databaseUrl = "jdbc:postgresql://127.0.0.1:5432/ct?user=myuser&password=mypassword";
}

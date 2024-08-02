package ct.server;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "ct-server-config", wrapperName = "CtServerConfig")
public class CtServerConfigModel {
    public short httpPort = 25581;
}

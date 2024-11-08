package cc.reconnected.server.config;

import cc.reconnected.server.RccServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            .create();
    private static final Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve(RccServer.MOD_ID + ".json");
    private static Config config = null;

    public static Config load() {
        if (!configFilePath.toFile().exists()) {
            config = new Config();
            save();
            return config;
        }
        try (var bf = new BufferedReader(new FileReader(configFilePath.toFile(), StandardCharsets.UTF_8))) {
            config = gson.fromJson(bf, Config.class);
            save();
        } catch (Exception e) {
            RccServer.LOGGER.error("Error loading the RccServer config file.", e);
        }
        return config;
    }

    public static void save() {
        var json = gson.toJson(config);
        try (var fw = new FileWriter(configFilePath.toFile(), StandardCharsets.UTF_8)) {
            fw.write(json);
        } catch (Exception e) {
            RccServer.LOGGER.error("Error saving the RccServer config file.", e);
        }
    }
}

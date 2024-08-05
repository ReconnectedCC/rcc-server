package ct.server;

import ct.server.database.DatabaseClient;
import ct.server.http.ServiceServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;


public class CtServer implements ModInitializer {
    public static final String MOD_ID = "ct-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ct.server.CtServerConfig CONFIG = ct.server.CtServerConfig.createAndLoad();

    private static float currentTps = 0;
    private static float currentMspt = 0;
    private static int currentPlayerCount = 0;

    public static CtServer getInstance() {
        return INSTANCE;
    }
    private static CtServer INSTANCE;

    private ServiceServer serviceServer;
    public ServiceServer serviceServer() {
        return serviceServer;
    }

    private DatabaseClient database;
    public DatabaseClient database() {
        return database;
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;

        LOGGER.info("Starting ct-server");

        try {
            database = new DatabaseClient();
        } catch(SQLException e) {
            LOGGER.error("Could not connect to the database", e);
        }

        try {
            serviceServer = new ServiceServer();
        } catch (IOException e) {
            LOGGER.error("Unable to start HTTP server", e);
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            currentMspt = server.getAverageTickTime();
            if (currentMspt != 0) {
                currentTps = Math.min(20, 1000 / currentMspt);
            }
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if(entity instanceof ServerPlayerEntity) {
                currentPlayerCount = world.getServer().getCurrentPlayerCount();
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            currentPlayerCount = server.getCurrentPlayerCount() - 1;
        });
    }

    public static float getTPS() {
        return currentTps;
    }

    public static float getMSPT() {
        return currentMspt;
    }

    public static int getPlayerCount() {
        return currentPlayerCount;
    }
}
package cc.reconnected.server;

import cc.reconnected.server.commands.RccCommand;
import cc.reconnected.server.database.DatabaseClient;
import cc.reconnected.server.database.PlayerData;
import cc.reconnected.server.database.PlayerTable;
import cc.reconnected.server.events.PlayerWelcome;
import cc.reconnected.server.http.ServiceServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;


public class RccServer implements ModInitializer {
    public static final String MOD_ID = "rcc-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final cc.reconnected.server.RccServerConfig CONFIG = cc.reconnected.server.RccServerConfig.createAndLoad();

    private static float currentTps = 0;
    private static float currentMspt = 0;
    private static int currentPlayerCount = 0;

    private static RccServer INSTANCE;
    public static RccServer getInstance() {
        return INSTANCE;
    }

    private ServiceServer serviceServer;
    public ServiceServer serviceServer() {
        return serviceServer;
    }

    private final DatabaseClient database = new DatabaseClient();
    public DatabaseClient database() {
        return database;
    }

    private final PlayerTable playerTable = new PlayerTable();
    public PlayerTable playerTable() {
        return playerTable;
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

    public RccServer() {
        INSTANCE = this;
    }

    @Override
    public void onInitialize() {

        LOGGER.info("Starting rcc-server");

        CommandRegistrationCallback.EVENT.register(RccCommand::register);

        try {
            // Jumpstart connection
            database.connection();
            playerTable.ensureDatabaseCreated();
        } catch (SQLException e) {
            LOGGER.error("Database error", e);
        }

        try {
            serviceServer = new ServiceServer();
        } catch (IOException e) {
            LOGGER.error("Unable to start HTTP server", e);
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            currentMspt = server.getTickTime();
            if (currentMspt != 0) {
                currentTps = Math.min(20, 1000 / currentMspt);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Stopping HTTP services");
            serviceServer.httpServer().stop(0);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            currentPlayerCount = server.getCurrentPlayerCount() + 1;
            var player = handler.getPlayer();
            var playerData = playerTable.getPlayerData(player.getUuid());
            if(playerData == null) {
                // new player!
                playerData = new PlayerData(handler.getPlayer().getUuid());
                playerData.firstJoinedDate(new Date());
                playerData.name(player.getName().getString());
                playerTable.updatePlayerData(playerData);

                PlayerWelcome.PLAYER_WELCOME.invoker().playerWelcome(player, playerData, server);

                // TODO: make it customizable via config
                broadcastMessage(server, Text.literal("Welcome " + player.getName().getString() + " to the server!").formatted(Formatting.LIGHT_PURPLE));
            } else {
                if (!playerData.name().equals(player.getName().getString())) {
                    playerData.name(player.getName().getString());
                    playerTable.updatePlayerData(playerData);
                }
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            currentPlayerCount = server.getCurrentPlayerCount() - 1;
        });
    }

    public void broadcastMessage(MinecraftServer server, Text message) {
        for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, false);
        }
    }
}
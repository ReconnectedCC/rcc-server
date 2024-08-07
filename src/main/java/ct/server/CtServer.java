package ct.server;

import ct.server.database.DatabaseClient;
import ct.server.database.PlayerData;
import ct.server.database.PlayerTable;
import ct.server.events.PlayerWelcome;
import ct.server.http.ServiceServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
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


public class CtServer implements ModInitializer {
    public static final String MOD_ID = "ct-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ct.server.CtServerConfig CONFIG = ct.server.CtServerConfig.createAndLoad();

    private static float currentTps = 0;
    private static float currentMspt = 0;
    private static int currentPlayerCount = 0;

    private static CtServer INSTANCE;
    public static CtServer getInstance() {
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

    public CtServer() {
        INSTANCE = this;
    }

    @Override
    public void onInitialize() {

        LOGGER.info("Starting ct-server");

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
            currentMspt = server.getAverageTickTime();
            if (currentMspt != 0) {
                currentTps = Math.min(20, 1000 / currentMspt);
            }
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
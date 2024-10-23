package cc.reconnected.server;

import cc.reconnected.server.commands.*;
import cc.reconnected.server.database.PlayerData;
import cc.reconnected.server.events.PlayerActivityEvents;
import cc.reconnected.server.events.PlayerWelcome;
import cc.reconnected.server.events.Ready;
import cc.reconnected.server.http.ServiceServer;
import cc.reconnected.server.trackers.AfkTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private LuckPerms luckPerms;

    public LuckPerms luckPerms() {
        return luckPerms;
    }

    private AfkTracker afkTracker;

    public AfkTracker afkTracker() {
        return afkTracker;
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

        CommandRegistrationCallback.EVENT.register(AfkCommand::register);
        CommandRegistrationCallback.EVENT.register(TellCommand::register);
        CommandRegistrationCallback.EVENT.register(ReplyCommand::register);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AfkCommand.register(dispatcher, registryAccess, environment);
            TellCommand.register(dispatcher, registryAccess, environment);
            ReplyCommand.register(dispatcher, registryAccess, environment);
            FlyCommand.register(dispatcher, registryAccess, environment);
            GodCommand.register(dispatcher, registryAccess, environment);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            luckPerms = LuckPermsProvider.get();
            afkTracker = new AfkTracker();
            Ready.READY.invoker().ready(server, luckPerms);

            if (CONFIG.enableHttpApi()) {
                try {
                    serviceServer = new ServiceServer();
                } catch (IOException e) {
                    LOGGER.error("Unable to start HTTP server", e);
                }
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            currentMspt = server.getTickTime();
            if (currentMspt != 0) {
                currentTps = Math.min(20, 1000 / currentMspt);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (CONFIG.enableHttpApi()) {
                LOGGER.info("Stopping HTTP services");
                serviceServer.httpServer().stop(0);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            currentPlayerCount = server.getCurrentPlayerCount() + 1;
            var player = handler.getPlayer();
            var playerData = PlayerData.getPlayer(player.getUuid());
            playerData.set(PlayerData.KEYS.username, player.getName().getString());
            var firstJoinedDate = playerData.getDate(PlayerData.KEYS.firstJoinedDate);
            boolean isNewPlayer = false;
            if (firstJoinedDate == null) {
                playerData.setDate(PlayerData.KEYS.firstJoinedDate, new Date());
                isNewPlayer = true;
            }
            if (isNewPlayer) {
                PlayerWelcome.PLAYER_WELCOME.invoker().playerWelcome(player, playerData, server);
                LOGGER.info("Player {} joined for the first time!", player.getName().getString());
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            currentPlayerCount = server.getCurrentPlayerCount() - 1;
        });

        PlayerActivityEvents.AFK.register((player, server) -> {
            LOGGER.info("{} is AFK. Active time: {} seconds.", player.getGameProfile().getName(), afkTracker.getActiveTime(player));

            var displayNameJson = Text.Serializer.toJson(player.getDisplayName());
            var displayName = JSONComponentSerializer.json().deserialize(displayNameJson);

            var message = MiniMessage.miniMessage().deserialize(CONFIG.afkMessage(),
                    Placeholder.component("displayname", displayName),
                    Placeholder.unparsed("username", player.getGameProfile().getName()),
                    Placeholder.unparsed("uuid", player.getUuid().toString())
            );

            broadcastMessage(server, message);
        });

        PlayerActivityEvents.AFK_RETURN.register((player, server) -> {
            LOGGER.info("{} is no longer AFK. Active time: {} seconds.", player.getGameProfile().getName(), afkTracker.getActiveTime(player));

            var displayNameJson = Text.Serializer.toJson(player.getDisplayName());
            var displayName = JSONComponentSerializer.json().deserialize(displayNameJson);

            var message = MiniMessage.miniMessage().deserialize(CONFIG.afkReturnMessage(),
                    Placeholder.component("displayname", displayName),
                    Placeholder.unparsed("username", player.getGameProfile().getName()),
                    Placeholder.unparsed("uuid", player.getUuid().toString())
            );

            broadcastMessage(server, message);
        });
    }

    public void broadcastMessage(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, false);
        }
    }

    public void broadcastMessage(MinecraftServer server, Component message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message);
        }
    }

    public boolean isPlayerAfk(PlayerEntity player) {
        return afkTracker.isPlayerAfk(player.getUuid());
    }

    public void setPlayerAfk(ServerPlayerEntity player, boolean afk) {
        afkTracker.setPlayerAfk(player, afk);
    }

    public int getActiveTime(ServerPlayerEntity player) {
        return afkTracker().getActiveTime(player);
    }
}
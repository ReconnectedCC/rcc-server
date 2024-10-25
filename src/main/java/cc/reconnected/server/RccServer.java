package cc.reconnected.server;

import cc.reconnected.server.commands.*;
import cc.reconnected.server.core.*;
import cc.reconnected.server.database.PlayerData;
import cc.reconnected.server.events.PlayerWelcome;
import cc.reconnected.server.events.Ready;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class RccServer implements ModInitializer {
    public static final String MOD_ID = "rcc-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final cc.reconnected.server.RccServerConfig CONFIG = cc.reconnected.server.RccServerConfig.createAndLoad();

    private static RccServer INSTANCE;

    public static RccServer getInstance() {
        return INSTANCE;
    }

    public RccServer() {
        INSTANCE = this;
    }

    private LuckPerms luckPerms;

    public LuckPerms luckPerms() {
        return luckPerms;
    }

    private volatile FabricServerAudiences adventure;

    public FabricServerAudiences adventure() {
        FabricServerAudiences ret = this.adventure;
        if (ret == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return ret;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Starting rcc-server");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.adventure = FabricServerAudiences.of(server));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.adventure = null);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RccCommand.register(dispatcher, registryAccess, environment);
            AfkCommand.register(dispatcher, registryAccess, environment);
            TellCommand.register(dispatcher, registryAccess, environment);
            ReplyCommand.register(dispatcher, registryAccess, environment);
            TeleportAskCommand.register(dispatcher, registryAccess, environment);
            TeleportAskHereCommand.register(dispatcher, registryAccess, environment);
            TeleportAcceptCommand.register(dispatcher, registryAccess, environment);
            TeleportDenyCommand.register(dispatcher, registryAccess, environment);
            BackCommand.register(dispatcher, registryAccess, environment);
            FlyCommand.register(dispatcher, registryAccess, environment);
            GodCommand.register(dispatcher, registryAccess, environment);
        });

        AfkTracker.register();
        TeleportTracker.register();
        BackTracker.register();
        TabList.register();
        HttpApiServer.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            luckPerms = LuckPermsProvider.get();
            Ready.READY.invoker().ready(server, luckPerms);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
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
}
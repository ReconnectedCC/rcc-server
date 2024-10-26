package cc.reconnected.server;

import cc.reconnected.server.api.events.RccEvents;
import cc.reconnected.server.commands.home.*;
import cc.reconnected.server.commands.misc.*;
import cc.reconnected.server.commands.spawn.*;
import cc.reconnected.server.commands.teleport.*;
import cc.reconnected.server.commands.tell.*;
import cc.reconnected.server.commands.warp.*;
import cc.reconnected.server.core.*;
import cc.reconnected.server.data.StateManager;
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
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class RccServer implements ModInitializer {
    public static final String MOD_ID = "rcc-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final cc.reconnected.server.RccServerConfig CONFIG = cc.reconnected.server.RccServerConfig.createAndLoad();

    public static final StateManager state = new StateManager();

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

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            state.register(server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve(RccServer.MOD_ID));
            this.adventure = FabricServerAudiences.of(server);
        });
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

            SetSpawnCommand.register(dispatcher, registryAccess, environment);
            SpawnCommand.register(dispatcher, registryAccess, environment);

            HomeCommand.register(dispatcher, registryAccess, environment);
            SetHomeCommand.register(dispatcher, registryAccess, environment);
            DeleteHomeCommand.register(dispatcher, registryAccess, environment);

            WarpCommand.register(dispatcher, registryAccess, environment);
            SetWarpCommand.register(dispatcher, registryAccess, environment);
            DeleteWarpCommand.register(dispatcher, registryAccess, environment);
        });

        AfkTracker.register();
        TeleportTracker.register();
        BackTracker.register();
        TabList.register();
        HttpApiServer.register();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            luckPerms = LuckPermsProvider.get();
            RccEvents.READY.invoker().onReady(server, luckPerms);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            var playerState = state.getPlayerState(player.getUuid());

            if (playerState.firstJoinedDate == null) {
                LOGGER.info("Player {} joined for the first time!", player.getGameProfile().getName());
                playerState.firstJoinedDate = new Date();
                RccEvents.WELCOME.invoker().onWelcome(player, server);
                var serverState = state.getServerState();
                var spawnPosition = serverState.spawn;

                if (spawnPosition != null) {
                    spawnPosition.teleport(player, false);
                }
            }

            if (playerState.username != null && !playerState.username.equals(player.getGameProfile().getName())) {
                LOGGER.info("Player {} has changed their username from {}", player.getGameProfile().getName(), playerState.username);
                RccEvents.USERNAME_CHANGE.invoker().onUsernameChange(player, playerState.username);
            }
            playerState.username = player.getGameProfile().getName();
            state.savePlayerState(player.getUuid(), playerState);
        });
    }

    public void broadcastMessage(MinecraftServer server, Component message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message);
        }
    }
}
package cc.reconnected.server;

import cc.reconnected.server.api.events.RccEvents;
import cc.reconnected.server.commands.admin.*;
import cc.reconnected.server.commands.home.DeleteHomeCommand;
import cc.reconnected.server.commands.home.HomeCommand;
import cc.reconnected.server.commands.home.SetHomeCommand;
import cc.reconnected.server.commands.misc.AfkCommand;
import cc.reconnected.server.commands.teleport.BackCommand;
import cc.reconnected.server.commands.misc.NearCommand;
import cc.reconnected.server.commands.spawn.SetSpawnCommand;
import cc.reconnected.server.commands.spawn.SpawnCommand;
import cc.reconnected.server.commands.teleport.TeleportAcceptCommand;
import cc.reconnected.server.commands.teleport.TeleportAskCommand;
import cc.reconnected.server.commands.teleport.TeleportAskHereCommand;
import cc.reconnected.server.commands.teleport.TeleportDenyCommand;
import cc.reconnected.server.commands.tell.ReplyCommand;
import cc.reconnected.server.commands.tell.TellCommand;
import cc.reconnected.server.commands.warp.DeleteWarpCommand;
import cc.reconnected.server.commands.warp.SetWarpCommand;
import cc.reconnected.server.commands.warp.WarpCommand;
import cc.reconnected.server.config.Config;
import cc.reconnected.server.config.ConfigManager;
import cc.reconnected.server.core.*;
import cc.reconnected.server.core.customChat.CustomChatMessage;
import cc.reconnected.server.data.StateManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.platform.fabric.FabricServerAudiences;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class RccServer implements ModInitializer {
    public static final String MOD_ID = "rcc-server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Config CONFIG = ConfigManager.load();

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

    public static MinecraftServer server;

    private volatile FabricServerAudiences adventure;

    public FabricServerAudiences adventure() {
        FabricServerAudiences ret = this.adventure;
        if (ret == null) {
            throw new IllegalStateException("Tried to access Adventure without a running server!");
        }
        return ret;
    }

    public static final RegistryKey<MessageType> CHAT_TYPE = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier(MOD_ID, "chat"));

    @Override
    public void onInitialize() {
        LOGGER.info("Starting rcc-server");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            RccServer.server = server;
            state.register(server.getSavePath(WorldSavePath.ROOT).resolve("data").resolve(RccServer.MOD_ID));
            this.adventure = FabricServerAudiences.of(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> this.adventure = null);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            RccCommand.register(dispatcher);

            AfkCommand.register(dispatcher);

            TellCommand.register(dispatcher);
            ReplyCommand.register(dispatcher);

            TeleportAskCommand.register(dispatcher);
            TeleportAskHereCommand.register(dispatcher);
            TeleportAcceptCommand.register(dispatcher);
            TeleportDenyCommand.register(dispatcher);
            BackCommand.register(dispatcher);

            FlyCommand.register(dispatcher);
            GodCommand.register(dispatcher);

            SetSpawnCommand.register(dispatcher);
            SpawnCommand.register(dispatcher);

            HomeCommand.register(dispatcher);
            SetHomeCommand.register(dispatcher);
            DeleteHomeCommand.register(dispatcher);

            WarpCommand.register(dispatcher);
            SetWarpCommand.register(dispatcher);
            DeleteWarpCommand.register(dispatcher);

            TimeBarCommand.register(dispatcher);
            RestartCommand.register(dispatcher);

            NearCommand.register(dispatcher);
        });

        AfkTracker.register();
        TeleportTracker.register();
        BackTracker.register();
        TabList.register();
        HttpApiServer.register();
        BossBarManager.register();
        AutoRestart.register();

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

    public void broadcastComponent(MinecraftServer server, Component message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message);
        }
    }

    public void broadcast(Text text) {
        server.getPlayerManager().broadcast(text, false);
    }

    public void sendChatAsPlayer(ServerPlayerEntity player, String message) {
        var msgType = server.getRegistryManager().get(RegistryKeys.MESSAGE_TYPE).getOrThrow(MessageType.CHAT);
        var signedMessage = SignedMessage.ofUnsigned(player.getUuid(), message);
        var pars = new MessageType.Parameters(msgType, Text.of(message), Text.of(message));

        var allowed = ServerMessageEvents.ALLOW_CHAT_MESSAGE.invoker().allowChatMessage(signedMessage, player, pars);
        if (!allowed)
            return;

        ServerMessageEvents.CHAT_MESSAGE.invoker().onChatMessage(signedMessage, player, pars);

        var formatted = CustomChatMessage.getFormattedMessage(signedMessage, player);
        for (var pl : server.getPlayerManager().getPlayerList()) {
            pl.sendMessage(formatted);
        }
    }

}
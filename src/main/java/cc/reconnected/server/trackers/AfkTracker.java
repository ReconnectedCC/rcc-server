package cc.reconnected.server.trackers;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.data.StateSaverAndLoader;
import cc.reconnected.server.database.PlayerData;
import cc.reconnected.server.events.PlayerActivityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.HashMap;
import java.util.UUID;

public class AfkTracker {
    private static final int cycleDelay = 1;
    private static final int absentTimeTrigger = RccServer.CONFIG.afkTimeTrigger() * 20; // seconds * 20 ticks

    private final HashMap<UUID, PlayerState> playerStates = new HashMap<>();

    public AfkTracker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % cycleDelay == 0) {
                updatePlayers(server);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final var player = handler.getPlayer();
            playerStates.put(player.getUuid(), new PlayerState(player, server.getTicks()));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            updatePlayerActiveTime(handler.getPlayer(), server.getTicks());
            playerStates.remove(handler.getPlayer().getUuid());

            // sync to LP
            //var activeTime = String.valueOf(getActiveTime(handler.getPlayer()));
            //var playerData = PlayerData.getPlayer(handler.getPlayer());

            //playerData.set(PlayerData.KEYS.activeTime, activeTime).join();
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            resetAfkState((ServerPlayerEntity) player, world.getServer());
            return ActionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            resetAfkState((ServerPlayerEntity) player, world.getServer());
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            resetAfkState((ServerPlayerEntity) player, world.getServer());
            return ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            resetAfkState((ServerPlayerEntity) player, world.getServer());
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            resetAfkState((ServerPlayerEntity) player, world.getServer());
            return TypedActionResult.pass(player.getStackInHand(hand));
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            resetAfkState(sender, sender.getServer());
            return true;
        });

        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            if (!source.isExecutedByPlayer())
                return true;
            resetAfkState(source.getPlayer(), source.getServer());
            return true;
        });
    }


    private void updatePlayer(ServerPlayerEntity player, MinecraftServer server) {
        var currentTick = server.getTicks();
        var playerState = playerStates.computeIfAbsent(player.getUuid(), uuid -> new PlayerState(player, currentTick));

        var oldPosition = playerState.position;
        var newPosition = new PlayerPosition(player);
        if (!oldPosition.equals(newPosition)) {
            playerState.position = newPosition;
            resetAfkState(player, server);
            return;
        }

        if (playerState.isAfk)
            return;

        if ((playerState.lastUpdate + absentTimeTrigger) <= currentTick) {
            // player is afk after 5 mins
            updatePlayerActiveTime(player, currentTick);
            playerState.isAfk = true;
            PlayerActivityEvents.AFK.invoker().onAfk(player, server);
        }
    }

    private void updatePlayerActiveTime(ServerPlayerEntity player, int currentTick) {
        var playerState = playerStates.get(player.getUuid());
        if(!playerState.isAfk) {
            var worldPlayerData = StateSaverAndLoader.getPlayerState(player);
            var interval = currentTick - playerState.activeStart;
            worldPlayerData.activeTime += interval / 20;
        }
    }

    private void updatePlayers(MinecraftServer server) {
        var players = server.getPlayerManager().getPlayerList();
        players.forEach(player -> {
            updatePlayer(player, server);
        });
    }

    private void resetAfkState(ServerPlayerEntity player, MinecraftServer server) {
        var playerState = playerStates.get(player.getUuid());
        playerState.lastUpdate = server.getTicks();
        if (playerState.isAfk) {
            playerState.isAfk = false;
            playerState.activeStart = server.getTicks();
            PlayerActivityEvents.AFK_RETURN.invoker().onAfkReturn(player, server);
        }
    }

    public static class PlayerPosition {
        public String dimension;
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;


        public boolean equals(PlayerPosition obj) {
            return x == obj.x && y == obj.y && z == obj.z
                    && yaw == obj.yaw && pitch == obj.pitch
                    && dimension.equals(obj.dimension);
        }

        public PlayerPosition(ServerPlayerEntity player) {
            dimension = player.getWorld().getRegistryKey().getValue().toString();
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            yaw = player.getYaw();
            pitch = player.getPitch();
        }
    }

    public static class PlayerState {
        public PlayerPosition position;
        public int lastUpdate;
        public boolean isAfk;
        public int activeStart;

        public PlayerState(ServerPlayerEntity player, int lastUpdate) {
            this.position = new PlayerPosition(player);
            this.lastUpdate = lastUpdate;
            this.isAfk = false;
            this.activeStart = lastUpdate;
        }
    }

    public boolean isPlayerAfk(UUID playerUuid) {
        if (!playerStates.containsKey(playerUuid)) {
            return false;
        }
        return playerStates.get(playerUuid).isAfk;
    }

    public void setPlayerAfk(ServerPlayerEntity player, boolean afk) {
        if (!playerStates.containsKey(player.getUuid())) {
            return;
        }

        var server = player.getWorld().getServer();

        if (afk) {
            playerStates.get(player.getUuid()).lastUpdate = -absentTimeTrigger - 20; // just to be sure
        } else {
            resetAfkState(player, server);
        }

        updatePlayer(player, server);
    }

    public int getActiveTime(ServerPlayerEntity player) {
        var worldPlayerData = StateSaverAndLoader.getPlayerState(player);
        return worldPlayerData.activeTime;
    }

}

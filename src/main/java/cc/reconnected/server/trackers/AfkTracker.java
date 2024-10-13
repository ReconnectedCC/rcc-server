package cc.reconnected.server.trackers;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.events.PlayerActivityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;

import java.util.HashMap;
import java.util.UUID;

public class AfkTracker {
    private static final int cycleDelay = 10;
    private static final int absentTimeTrigger = 300 * 20; // 5 mins (* 20 ticks)
    private final HashMap<UUID, PlayerPosition> playerPositions = new HashMap<>();
    private final HashMap<UUID, Integer> playerLastUpdate = new HashMap<>();
    private final HashMap<UUID, Boolean> playerAfkStates = new HashMap<>();

    public AfkTracker() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % cycleDelay == 0) {
                updatePlayers(server);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            final var player = handler.getPlayer();
            var playerPosition = new PlayerPosition(player);
            playerPositions.put(player.getUuid(), playerPosition);
            playerLastUpdate.put(player.getUuid(), server.getTicks());
            playerAfkStates.put(player.getUuid(), false);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            playerPositions.remove(handler.getPlayer().getUuid());
            playerLastUpdate.remove(handler.getPlayer().getUuid());
            playerAfkStates.remove(handler.getPlayer().getUuid());
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
            if(!source.isExecutedByPlayer())
                return true;
            resetAfkState(source.getPlayer(), source.getServer());
            return true;
        });
    }

    public void updatePlayers(MinecraftServer server) {
        var players = server.getPlayerManager().getPlayerList();
        var currentTick = server.getTicks();
        players.forEach(player -> {
            if (!playerPositions.containsKey(player.getUuid())) {
                playerPositions.put(player.getUuid(), new PlayerPosition(player));
                return;
            }
            var oldPosition = playerPositions.get(player.getUuid());
            var newPosition = new PlayerPosition(player);
            if (!oldPosition.equals(newPosition)) {
                playerPositions.put(player.getUuid(), newPosition);
                resetAfkState(player, server);
                return;
            }

            if (playerAfkStates.get(player.getUuid())) {
                return;
            }

            if ((playerLastUpdate.get(player.getUuid()) + absentTimeTrigger) <= currentTick) {
                // player is afk after 5 mins
                playerAfkStates.put(player.getUuid(), true);
                PlayerActivityEvents.AFK.invoker().onAfk(player, server);
            }
        });
    }

    private void resetAfkState(ServerPlayerEntity player, MinecraftServer server) {
        playerLastUpdate.put(player.getUuid(), server.getTicks());
        if (playerAfkStates.get(player.getUuid())) {
            PlayerActivityEvents.AFK_RETURN.invoker().onAfkReturn(player, server);
            playerAfkStates.put(player.getUuid(), false);
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
}

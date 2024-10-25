package cc.reconnected.server.core;

import cc.reconnected.server.events.PlayerTeleport;
import cc.reconnected.server.struct.ServerPosition;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BackTracker {
    public static final ConcurrentHashMap<UUID, ServerPosition> lastPlayerPositions = new ConcurrentHashMap<>();

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            BackTracker.lastPlayerPositions.remove(handler.getPlayer().getUuid());
        });

        PlayerTeleport.EVENT.register((player, origin, destination) -> {
            lastPlayerPositions.put(player.getUuid(), origin);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            lastPlayerPositions.put(oldPlayer.getUuid(), new ServerPosition(oldPlayer));
        });
    }
}

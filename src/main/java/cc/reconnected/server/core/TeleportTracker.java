package cc.reconnected.server.core;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.events.PlayerTeleport;
import cc.reconnected.server.struct.ServerPosition;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TeleportTracker {
    public static final ConcurrentHashMap<UUID, ConcurrentLinkedDeque<TeleportRequest>> teleportRequests = new ConcurrentHashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            teleportRequests.forEach((recipient, requestList) -> {
                requestList.forEach(request -> {
                    if (request.remainingTicks-- == 0) {
                        requestList.remove(request);
                    }
                });
            });
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            teleportRequests.put(handler.getPlayer().getUuid(), new ConcurrentLinkedDeque<>());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            TeleportTracker.teleportRequests.remove(handler.getPlayer().getUuid());
        });
    }

    public static Component makeButton(ComponentLike text, ComponentLike hoverText, String command) {
        return Component.empty()
                .append(Component.text("["))
                .append(text)
                .append(Component.text("]"))
                .color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static void teleport(ServerPlayerEntity sourcePlayer, ServerPlayerEntity targetPlayer) {
        PlayerTeleport.EVENT.invoker().teleport(sourcePlayer, new ServerPosition(sourcePlayer), new ServerPosition(targetPlayer));

        sourcePlayer.teleport(
                targetPlayer.getServerWorld(),
                targetPlayer.getX(),
                targetPlayer.getY(),
                targetPlayer.getZ(),
                targetPlayer.getYaw(),
                targetPlayer.getPitch()
        );
    }

    public static class TeleportRequest {
        public UUID requestId = UUID.randomUUID();
        public UUID player;
        public UUID target;
        public int remainingTicks;

        public TeleportRequest(UUID player, UUID target) {
            this.player = player;
            this.target = target;
            // Seconds in config per 20 ticks
            this.remainingTicks = RccServer.CONFIG.teleportRequestTimeout() * 20;
        }

        public void expire() {
            remainingTicks = 0;
        }
    }
}

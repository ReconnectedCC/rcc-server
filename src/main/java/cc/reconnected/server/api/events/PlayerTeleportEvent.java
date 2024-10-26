package cc.reconnected.server.api.events;

import cc.reconnected.server.struct.ServerPosition;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerTeleportEvent {
    Event<PlayerTeleportEvent> EVENT = EventFactory.createArrayBacked(PlayerTeleportEvent.class,
            (listeners) -> (player, origin, destination) -> {
                for (PlayerTeleportEvent listener : listeners) {
                    listener.teleport(player, origin, destination);
                }
            });

    void teleport(ServerPlayerEntity player, ServerPosition origin, ServerPosition destination);
}

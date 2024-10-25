package cc.reconnected.server.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerWelcome {
    Event<PlayerWelcome> PLAYER_WELCOME = EventFactory.createArrayBacked(PlayerWelcome.class,
            (listeners) -> (player, server) -> {
                for (PlayerWelcome listener : listeners) {
                    listener.playerWelcome(player, server);
                }
            });

    void playerWelcome(ServerPlayerEntity player, MinecraftServer server);
}

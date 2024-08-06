package ct.server.events;

import ct.server.database.PlayerData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerWelcome {
    Event<PlayerWelcome> PLAYER_WELCOME = EventFactory.createArrayBacked(PlayerWelcome.class,
            (listeners) -> (player, playerData, server) -> {
                for (PlayerWelcome listener : listeners) {
                    listener.playerWelcome(player, playerData, server);
                }
            });

    void playerWelcome(ServerPlayerEntity player, PlayerData playerData, MinecraftServer server);
}

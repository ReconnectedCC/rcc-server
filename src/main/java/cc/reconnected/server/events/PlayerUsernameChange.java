package cc.reconnected.server.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerUsernameChange {
    Event<PlayerUsernameChange> PLAYER_USERNAME_CHANGE = EventFactory.createArrayBacked(PlayerUsernameChange.class,
            (listeners) -> (player, previousUsername) -> {
                for (PlayerUsernameChange listener : listeners) {
                    listener.changeUsername(player, previousUsername);
                }
            });

    void changeUsername(ServerPlayerEntity player, String previousUsername);
}

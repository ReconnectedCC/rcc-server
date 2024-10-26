package cc.reconnected.server.api.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.luckperms.api.LuckPerms;
import net.minecraft.server.MinecraftServer;

public interface Ready {
    Event<Ready> READY = EventFactory.createArrayBacked(Ready.class,
            (listeners) -> (server, luckPerms) -> {
                for (Ready listener : listeners) {
                    listener.ready(server, luckPerms);
                }
            });

    void ready(MinecraftServer server, LuckPerms luckPerms);
}

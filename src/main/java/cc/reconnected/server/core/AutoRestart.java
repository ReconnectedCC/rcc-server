package cc.reconnected.server.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class AutoRestart {
    private static MinecraftServer server;
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);

        
    }

    private static void schedule() {

    }
}

package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomConnectionMessage {
    public static Text onJoin(ServerPlayerEntity player) {
        var playerContext = PlaceholderContext.of(player);
        return Components.parse(
                RccServer.CONFIG.textFormats.joinFormat,
                playerContext
        );
    }

    public static Text onJoinRenamed(ServerPlayerEntity player, String previousName) {
        var playerContext = PlaceholderContext.of(player);
        return Components.parse(
                RccServer.CONFIG.textFormats.joinRenamedFormat,
                playerContext,
                Map.of("previousName", Text.of(previousName))
        );
    }

    public static Text onLeave(ServerPlayerEntity player) {
        var playerContext = PlaceholderContext.of(player);
        return Components.parse(
                RccServer.CONFIG.textFormats.leaveFormat,
                playerContext
        );
    }
}

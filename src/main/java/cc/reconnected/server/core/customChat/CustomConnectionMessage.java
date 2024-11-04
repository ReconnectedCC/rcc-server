package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CustomConnectionMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Text onJoin(ServerPlayerEntity player) {
        var joinMessage = miniMessage.deserialize(RccServer.CONFIG.customChatFormat.joinFormat(),
                TagResolver.resolver(
                        Placeholder.component("display_name", player.getDisplayName())
                ));

        return Components.toText(joinMessage);
    }

    public static Text onJoinRenamed(ServerPlayerEntity player, String previousName) {
        var joinMessage = miniMessage.deserialize(RccServer.CONFIG.customChatFormat.joinRenamedFormat(),
                TagResolver.resolver(
                        Placeholder.component("display_name", player.getDisplayName()),
                        Placeholder.component("previous_name", Text.of(previousName))
                ));

        return Components.toText(joinMessage);
    }

    public static Text onLeave(ServerPlayerEntity player) {
        var leaveMessage = miniMessage.deserialize(RccServer.CONFIG.customChatFormat.leaveFormat(),
                TagResolver.resolver(
                        Placeholder.component("display_name", player.getDisplayName())
                ));

        return Components.toText(leaveMessage);
    }
}

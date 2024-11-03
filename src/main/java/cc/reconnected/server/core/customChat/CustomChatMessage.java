package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.parser.MarkdownParser;
import cc.reconnected.server.util.Components;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CustomChatMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void sendChatMessage(ServerPlayerEntity receiver, SignedMessage message, MessageType.Parameters params) {
        var playerUuid = message.link().sender();
        var player = RccServer.server.getPlayerManager().getPlayer(playerUuid);

        Text messageText;
        if (RccServer.CONFIG.customChatFormat.enableMarkdown()) {
            messageText = MarkdownParser.defaultParser.parseNode(message.getSignedContent()).toText();
        } else {
            messageText = message.getContent();
        }

        var component = miniMessage.deserialize(RccServer.CONFIG.customChatFormat.chatFormat(), TagResolver.resolver(
                Placeholder.component("display_name", player.getDisplayName()),
                Placeholder.component("message", messageText)
        ));

        var text = Components.toText(component);

        var msgType = RccServer.server.getRegistryManager().get(RegistryKeys.MESSAGE_TYPE).getOrThrow(RccServer.CHAT_TYPE);
        var newParams = new MessageType.Parameters(msgType, text, null);

        receiver.networkHandler.sendChatMessage(message, newParams);
    }
}

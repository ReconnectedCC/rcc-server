package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomChatMessage {
    public static void sendChatMessage(ServerPlayerEntity receiver, SignedMessage message, MessageType.Parameters params) {
        var playerUuid = message.link().sender();
        var player = RccServer.server.getPlayerManager().getPlayer(playerUuid);

        var text = getFormattedMessage(message, player);

        var msgType = RccServer.server.getRegistryManager().get(RegistryKeys.MESSAGE_TYPE).getOrThrow(RccServer.CHAT_TYPE);
        var newParams = new MessageType.Parameters(msgType, text, null);

        receiver.networkHandler.sendChatMessage(message, newParams);
    }

    public static Text getFormattedMessage(SignedMessage message, ServerPlayerEntity player) {
        Text messageText = Components.chat(message, player);

        var playerContext = PlaceholderContext.of(player);
        var text = Components.parse(
                RccServer.CONFIG.textFormats.chatFormat,
                playerContext,
                Map.of(
                        "message", messageText
                )
        );
        return text;
    }
}

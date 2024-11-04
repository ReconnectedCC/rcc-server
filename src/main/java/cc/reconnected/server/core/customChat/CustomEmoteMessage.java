package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomEmoteMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void sendEmoteMessage(ServerPlayerEntity receiver, SignedMessage message, MessageType.Parameters params) {
        var playerUuid = message.link().sender();
        var player = RccServer.server.getPlayerManager().getPlayer(playerUuid);

        Text messageText = Utils.formatChatMessage(message, player);

        var placeholders = Map.of(
                "message", messageText,
                "player", player.getDisplayName()
        );

        var format = TextParserUtils.formatText(RccServer.CONFIG.textFormats.emoteFormat);
        var text = Placeholders.parseText(format, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);

        var msgType = RccServer.server.getRegistryManager().get(RegistryKeys.MESSAGE_TYPE).getOrThrow(RccServer.CHAT_TYPE);
        var newParams = new MessageType.Parameters(msgType, text, null);

        receiver.networkHandler.sendChatMessage(message, newParams);
    }
}

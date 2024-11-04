package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomConnectionMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Text onJoin(ServerPlayerEntity player) {
        var placeholders = Map.of(
                "player", player.getDisplayName()
        );

        var format = TextParserUtils.formatText(RccServer.CONFIG.textFormats.joinFormat);
        return Placeholders.parseText(format, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }

    public static Text onJoinRenamed(ServerPlayerEntity player, String previousName) {
        var placeholders = Map.of(
                "previousName", Text.of(previousName),
                "player", player.getDisplayName()
        );

        var format = TextParserUtils.formatText(RccServer.CONFIG.textFormats.joinRenamedFormat);
        return Placeholders.parseText(format, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }

    public static Text onLeave(ServerPlayerEntity player) {
        var placeholders = Map.of(
                "player", player.getDisplayName()
        );

        var format = TextParserUtils.formatText(RccServer.CONFIG.textFormats.leaveFormat);
        return Placeholders.parseText(format, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }
}

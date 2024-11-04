package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomDeathMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Text onDeath(ServerPlayerEntity player, DamageTracker instance) {
        var deathMessage = instance.getDeathMessage();

        var placeholders = Map.of(
                "message", deathMessage,
                "player", player.getDisplayName()
        );

        var format = TextParserUtils.formatText(RccServer.CONFIG.textFormats.deathFormat);
        return Placeholders.parseText(format, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }
}

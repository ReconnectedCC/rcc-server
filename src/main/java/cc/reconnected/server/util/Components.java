package cc.reconnected.server.util;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.parser.MarkdownParser;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Map;

public class Components {
    @Deprecated
    public static Component makeButton(ComponentLike text, ComponentLike hoverText, String command) {
        return Component.empty()
                .append(Component.text("["))
                .append(text)
                .append(Component.text("]"))
                .color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Text button(Text label, Text hoverText, String command) {
        var format = RccServer.CONFIG.textFormats.commands.common.button;
        var placeholders = Map.of(
                "label", label,
                "hoverText", hoverText,
                "command", Text.of(command)
        );

        format = format.replace("{{command}}", command);
        var text = TextParserUtils.formatText(format);
        return Placeholders.parseText(text, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }

    public static Text button(String label, String hoverText, String command) {
        var btn = button(
                TextParserUtils.formatText(label),
                TextParserUtils.formatText(hoverText),
                command
        );

        return btn;
    }

    public static MutableText toText(Component component) {
        var json = JSONComponentSerializer.json().serialize(component);
        return Text.Serializer.fromJson(json);
    }

    public static Text parse(String text) {
        return TextParserUtils.formatText(text);
    }

    public static Text parse(TextNode textNode, PlaceholderContext context, Map<String, Text> placeholders) {
        var predefinedNode = Placeholders.parseNodes(textNode, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
        return Placeholders.parseText(predefinedNode, context);
    }

    public static Text parse(Text text, PlaceholderContext context, Map<String, Text> placeholders) {
        return parse(TextNode.convert(text), context, placeholders);
    }

    public static Text parse(String text, PlaceholderContext context, Map<String, Text> placeholders) {
        return parse(parse(text), context, placeholders);
    }

    public static Text parse(String text, PlaceholderContext context) {
        return parse(parse(text), context, Map.of());
    }

    public static Text parse(String text, Map<String, Text> placeholders) {
        return Placeholders.parseText(parse(text), PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
    }

    public static Text chat(SignedMessage message, ServerPlayerEntity player) {
        var luckperms = RccServer.getInstance().luckPerms();

        var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
        var allowAdvancedChatFormat = permissions.checkPermission("rcc.chat.advanced").asBoolean();

        return chat(message.getSignedContent(), allowAdvancedChatFormat);
    }

    public static Text chat(String message, ServerPlayerEntity player) {
        var luckperms = RccServer.getInstance().luckPerms();

        var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
        var allowAdvancedChatFormat = permissions.checkPermission("rcc.chat.advanced").asBoolean();

        return chat(message, allowAdvancedChatFormat);
    }

    public static Text chat(String message, boolean allowAdvancedChatFormat) {
        var enableMarkdown = RccServer.CONFIG.chat.enableChatMarkdown;

        for(var repl : RccServer.CONFIG.chat.replacements.entrySet() ) {
            message = message.replace(repl.getKey(), repl.getValue());
        }

        if(!allowAdvancedChatFormat && !enableMarkdown) {
            return Text.of(message);
        }

        NodeParser parser;
        if(allowAdvancedChatFormat) {
            parser = NodeParser.merge(TextParserV1.DEFAULT, MarkdownParser.defaultParser);
        } else {
            parser = MarkdownParser.defaultParser;
        }

        return parser.parseNode(message).toText();
    }

    public static Text chat(String message, ServerCommandSource source) {
        if(source.isExecutedByPlayer())
            return chat(message, source.getPlayer());
        return chat(message, true);
    }
}

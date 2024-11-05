package cc.reconnected.server.util;

import cc.reconnected.server.RccServer;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
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
        var parser = TextParserV1.DEFAULT;
        var btn = button(
                parser.parseNode(label).toText(),
                parser.parseNode(hoverText).toText(),
                command
        );

        return btn;
    }

    public static MutableText toText(Component component) {
        var json = JSONComponentSerializer.json().serialize(component);
        return Text.Serializer.fromJson(json);
    }

    public static Text parse(TextNode textNode, PlaceholderContext context, Map<String, Text> placeholders) {
        var predefinedNode = Placeholders.parseNodes(textNode, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
        return Placeholders.parseText(predefinedNode, context);
    }

    public static Text parse(Text text, PlaceholderContext context, Map<String, Text> placeholders) {
        return parse(TextNode.convert(text), context, placeholders);
    }

    public static Text parse(String text, PlaceholderContext context, Map<String, Text> placeholders) {
        return parse(TextParserUtils.formatNodes(text), context, placeholders);
    }

    public static Text parse(String text, PlaceholderContext context) {
        return parse(TextParserUtils.formatNodes(text), context, Map.of());
    }
}

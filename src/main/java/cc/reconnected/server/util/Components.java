package cc.reconnected.server.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.text.Text;

public class Components {
    public static Component makeButton(ComponentLike text, ComponentLike hoverText, String command) {
        return Component.empty()
                .append(Component.text("["))
                .append(text)
                .append(Component.text("]"))
                .color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static Text toText(Component component) {
        var json = JSONComponentSerializer.json().serialize(component);
        return Text.Serializer.fromJson(json);
    }
}

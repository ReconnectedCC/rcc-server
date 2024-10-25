package cc.reconnected.server.tablist;

import cc.reconnected.server.RccServer;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.text.Text;

public class TabList {
    public static void register() {
        if (!RccServer.CONFIG.enableTabList())
            return;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var phase = (Math.sin((server.getTicks() * Math.PI) / 20) + 1) / 2d;
            var minimessage = MiniMessage.miniMessage();

            server.getPlayerManager().getPlayerList().forEach(player -> {
                var playerContext = PlaceholderContext.of(player);
                Component headerComponent = Component.empty();
                for (int i = 0; i < RccServer.CONFIG.tabHeader().size(); i++) {
                    var line = RccServer.CONFIG.tabHeader().get(i);
                    line = line.replace("{phase}", String.valueOf(phase));
                    if (i > 0) {
                        headerComponent = headerComponent.appendNewline();
                    }

                    headerComponent = headerComponent.append(minimessage.deserialize(line));
                }

                Component footerComponent = Component.empty();
                for (int i = 0; i < RccServer.CONFIG.tabFooter().size(); i++) {
                    var line = RccServer.CONFIG.tabFooter().get(i);
                    line = line.replace("{phase}", String.valueOf(phase));
                    if (i > 0) {
                        footerComponent = footerComponent.appendNewline();
                    }

                    footerComponent = footerComponent.append(minimessage.deserialize(line));
                }

                var parsedHeader = Placeholders.parseText(toText(headerComponent), playerContext);
                var parsedFooter = Placeholders.parseText(toText(footerComponent), playerContext);

                var audience = RccServer.getInstance().adventure().player(player.getUuid());
                audience.sendPlayerListHeaderAndFooter(parsedHeader, parsedFooter);
            });
        });

    }

    public static Text toText(Component component) {
        var json = JSONComponentSerializer.json().serialize(component);
        return Text.Serializer.fromJson(json);
    }
}

package cc.reconnected.server.core;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.luckperms.api.model.group.Group;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class CustomNameFormat {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static MutableText getNameForPlayer(ServerPlayerEntity player) {
        var formats = RccServer.CONFIG.customName.formats().entrySet();
        var lp = RccServer.getInstance().luckPerms();
        var playerContext = PlaceholderContext.of(player);

        var user = lp.getPlayerAdapter(ServerPlayerEntity.class).getUser(player);

        var groups = user.getInheritedGroups(user.getQueryOptions()).stream().map(Group::getName).toList();

        String format = null;
        for (var entry : formats) {
            if (groups.contains(entry.getKey())) {
                format = entry.getValue();
                break;
            }
        }

        if (format == null) {
            format = "<username>";
        }

        var displayName = miniMessage.deserialize(format, TagResolver.resolver(
                Placeholder.parsed("username", player.getGameProfile().getName())
        ));

        return Placeholders.parseText(Components.toText(displayName), playerContext).copy();
    }
}

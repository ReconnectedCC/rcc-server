package cc.reconnected.server.core;

import cc.reconnected.server.RccServer;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

public class CustomNameFormat {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    public static MutableText getNameForPlayer(ServerPlayerEntity player) {
        var formats = RccServer.CONFIG.textFormats.nameFormats;
        var lp = RccServer.getInstance().luckPerms();
        var playerContext = PlaceholderContext.of(player);

        var user = lp.getPlayerAdapter(ServerPlayerEntity.class).getUser(player);

        var groups = user.getInheritedGroups(user.getQueryOptions()).stream().map(Group::getName).toList();

        String format = null;
        for (var f : formats) {
            if (groups.contains(f.group())) {
                format = f.format();
                break;
            }
        }

        if (format == null) {
            format = "%player:name%";
        }

        var output = TextParserUtils.formatText(format);
        return Placeholders.parseText(output, playerContext).copy();
    }
}

package cc.reconnected.server.commands.teleport;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.TeleportTracker;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;

public class TeleportAskCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var node = dispatcher.register(literal("tpa")
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var playerManager = context.getSource().getServer().getPlayerManager();
                            return CommandSource.suggestMatching(
                                    playerManager.getPlayerNames(),
                                    builder);
                        })
                        .executes(context -> {
                            execute(context);
                            return 1;
                        })));

        dispatcher.register(literal("tpask").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
            return;
        }

        var server = source.getServer();
        var player = source.getPlayer();
        var targetName = StringArgumentType.getString(context, "player");
        var playerManager = server.getPlayerManager();
        var target = playerManager.getPlayer(targetName);
        var playerContext = PlaceholderContext.of(player);
        var parser = TextParserV1.DEFAULT;
        if (target == null) {
            var placeholders = Map.of(
                    "targetPlayer", Text.of(targetName)
            );
            var text = parser.parseNode(RccServer.CONFIG.textFormats.commands.teleportRequest.playerNotFound);
            source.sendFeedback(() -> Placeholders.parseText(text, playerContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders), false);
            return;
        }

        var request = new TeleportTracker.TeleportRequest(player.getUuid(), target.getUuid());
        var targetRequests = TeleportTracker.teleportRequests.get(target.getUuid());
        targetRequests.addLast(request);
        var targetContext = PlaceholderContext.of(target);
        var placeholders = Map.of(
                "requesterPlayer", player.getDisplayName(),
                "acceptButton", Components.button(
                        RccServer.CONFIG.textFormats.commands.common.accept,
                        RccServer.CONFIG.textFormats.commands.teleportRequest.hoverAccept,
                        "/tpaccept " + request.requestId),
                "refuseButton", Components.button(
                        RccServer.CONFIG.textFormats.commands.common.refuse,
                        RccServer.CONFIG.textFormats.commands.teleportRequest.hoverRefuse,
                        "/tpdeny " + request.requestId)
        );

        var requestText = Placeholders.parseText(
                parser.parseNode(RccServer.CONFIG.textFormats.commands.teleportRequest.pendingTeleport),
                targetContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders
        );

        target.sendMessage(requestText);

        source.sendFeedback(() -> parser.parseNode(RccServer.CONFIG.textFormats.commands.teleportRequest.requestSent).toText(), false);
    }

}

package cc.reconnected.server.commands.teleport;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.TeleportTracker;
import cc.reconnected.server.struct.ServerPosition;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportAcceptCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var node = dispatcher.register(literal("tpaccept")
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var player = context.getSource().getPlayer();
                    var playerUuid = player.getUuid();
                    var playerRequests = TeleportTracker.teleportRequests.get(playerUuid);
                    var playerContext = PlaceholderContext.of(player);

                    var request = playerRequests.pollLast();

                    if (request == null) {
                        context.getSource().sendFeedback(() -> Components.parse(
                                RccServer.CONFIG.textFormats.commands.teleportRequest.noPending,
                                playerContext
                        ), false);
                        return 1;
                    }

                    execute(context, request);

                    return 1;
                })
                .then(argument("uuid", UuidArgumentType.uuid())
                        .executes(context -> {
                            if (!context.getSource().isExecutedByPlayer()) {
                                context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                                return 1;
                            }

                            var player = context.getSource().getPlayer();
                            var uuid = UuidArgumentType.getUuid(context, "uuid");
                            var playerUuid = player.getUuid();
                            var playerRequests = TeleportTracker.teleportRequests.get(playerUuid);
                            var playerContext = PlaceholderContext.of(player);

                            var request = playerRequests.stream().filter(req -> req.requestId.equals(uuid)).findFirst().orElse(null);
                            if (request == null) {
                                context.getSource().sendFeedback(() -> Components.parse(
                                        RccServer.CONFIG.textFormats.commands.teleportRequest.unavailable,
                                        playerContext
                                ), false);
                                return 1;
                            }

                            execute(context, request);

                            return 1;
                        })));

        dispatcher.register(literal("tpyes").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context, TeleportTracker.TeleportRequest request) {
        var source = context.getSource();
        request.expire();

        var player = source.getPlayer();

        var playerManager = context.getSource().getServer().getPlayerManager();

        var sourcePlayer = playerManager.getPlayer(request.player);
        var targetPlayer = playerManager.getPlayer(request.target);

        var playerContext = PlaceholderContext.of(player);

        if (sourcePlayer == null || targetPlayer == null) {
            context.getSource().sendFeedback(() -> Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.playerUnavailable,
                    playerContext
            ), false);
            return;
        }

        if (player.getUuid().equals(request.target)) {
            var sourceContext = PlaceholderContext.of(sourcePlayer);
            // accepted a tpa from other to self
            context.getSource().sendFeedback(() -> Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.requestAcceptedResult,
                    playerContext
            ), false);
            sourcePlayer.sendMessage(Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.teleporting,
                    sourceContext
            ), false);
        } else {
            var targetContext = PlaceholderContext.of(targetPlayer);
            // accepted a tpa from self to other
            context.getSource().sendFeedback(() -> Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.teleporting,
                    playerContext
            ), false);

            targetPlayer.sendMessage(Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.requestAccepted,
                    targetContext,
                    Map.of("player", sourcePlayer.getDisplayName())
            ), false);
        }

        var targetPosition = new ServerPosition(targetPlayer);
        targetPosition.teleport(sourcePlayer);
    }
}

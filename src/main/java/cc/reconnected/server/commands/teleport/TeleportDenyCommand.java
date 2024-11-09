package cc.reconnected.server.commands.teleport;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.TeleportTracker;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportDenyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var node = dispatcher.register(literal("tpdeny")
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
                            var player = context.getSource().getPlayerOrThrow();
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

        dispatcher.register(literal("tpno").redirect(node));
        dispatcher.register(literal("tprefuse").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context, TeleportTracker.TeleportRequest request) {
        var source = context.getSource();
        request.expire();

        var player = source.getPlayer();
        var playerManager = context.getSource().getServer().getPlayerManager();
        var playerContext = PlaceholderContext.of(player);

        ServerPlayerEntity otherPlayer = null;
        if (player.getUuid().equals(request.target)) {
            otherPlayer = playerManager.getPlayer(request.player);
        } else if (player.getUuid().equals(request.player)) {
            otherPlayer = playerManager.getPlayer(request.target);
        }

        if (otherPlayer != null) {
            var otherContext = PlaceholderContext.of(otherPlayer);
            otherPlayer.sendMessage(Components.parse(
                    RccServer.CONFIG.textFormats.commands.teleportRequest.requestRefused,
                    otherContext,
                    Map.of("player", player.getDisplayName())
            ), false);
        }

        context.getSource().sendFeedback(() -> Components.parse(
                RccServer.CONFIG.textFormats.commands.teleportRequest.requestRefusedResult,
                playerContext
        ), false);
    }
}

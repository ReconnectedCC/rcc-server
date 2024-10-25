package cc.reconnected.server.commands;

import cc.reconnected.server.core.TeleportTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.*;

public class TeleportAcceptCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var node = dispatcher.register(literal("tpaccept")
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var playerUuid = context.getSource().getPlayer().getUuid();
                    var playerRequests = TeleportTracker.teleportRequests.get(playerUuid);


                    var request = playerRequests.pollLast();

                    if (request == null) {
                        context.getSource().sendFeedback(() -> Text.literal("You have no pending teleport requests.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
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

                            var uuid = UuidArgumentType.getUuid(context, "uuid");
                            var playerUuid = context.getSource().getPlayer().getUuid();
                            var playerRequests = TeleportTracker.teleportRequests.get(playerUuid);

                            var request = playerRequests.stream().filter(req -> req.requestId.equals(uuid)).findFirst().orElse(null);
                            if (request == null) {
                                context.getSource().sendFeedback(() -> Text.literal("This request expired or is no longer available.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
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

        if (sourcePlayer == null || targetPlayer == null) {
            context.getSource().sendFeedback(() -> Text.literal("The other player is no longer available.").formatted(Formatting.RED), false);
            return;
        }

        if (player.getUuid().equals(request.target)) {
            // accepted a tpa from other to self
            context.getSource().sendFeedback(() -> Text.literal("Teleport request accepted.").formatted(Formatting.GREEN), false);
            sourcePlayer.sendMessage(Text.literal("Teleporting...").formatted(Formatting.GOLD), false);
        } else {
            // accepted a tpa from self to other
            context.getSource().sendFeedback(() -> Text.literal("Teleporting...").formatted(Formatting.GOLD), false);
            targetPlayer.sendMessage(Text.empty().append(player.getDisplayName()).append(Text.literal(" accepted your teleport request.").formatted(Formatting.GREEN)), false);
        }

        TeleportTracker.teleport(sourcePlayer, targetPlayer);
    }
}

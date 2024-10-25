package cc.reconnected.server.commands;

import cc.reconnected.server.core.TeleportTracker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportDenyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var node = dispatcher.register(literal("tpdeny")
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

        dispatcher.register(literal("tpno").redirect(node));
        dispatcher.register(literal("tprefuse").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context, TeleportTracker.TeleportRequest request) {
        var source = context.getSource();
        request.expire();

        var player = source.getPlayer();

        var playerManager = context.getSource().getServer().getPlayerManager();

        ServerPlayerEntity otherPlayer = null;
        if (player.getUuid().equals(request.target)) {
            otherPlayer = playerManager.getPlayer(request.player);
        } else if (player.getUuid().equals(request.player)) {
            otherPlayer = playerManager.getPlayer(request.target);
        }

        if (otherPlayer != null) {
            otherPlayer.sendMessage(Text.empty().append(player.getDisplayName()).append(Text.literal(" denied your teleport request.").formatted(Formatting.RED)));
        }
        context.getSource().sendFeedback(() -> Text.literal("You denied the teleport request.").formatted(Formatting.GOLD), false);
    }
}

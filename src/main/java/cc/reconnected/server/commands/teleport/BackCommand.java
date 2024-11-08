package cc.reconnected.server.commands.teleport;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.BackTracker;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("back")
                .requires(Permissions.require("rcc.command.back", true))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var player = context.getSource().getPlayer();
                    var playerContext = PlaceholderContext.of(player);

                    var lastPosition = BackTracker.lastPlayerPositions.get(player.getUuid());
                    if (lastPosition == null) {
                        context.getSource().sendFeedback(() -> Components.parse(
                                RccServer.CONFIG.textFormats.commands.back.noPosition,
                                playerContext
                        ), false);
                        return 1;
                    }

                    context.getSource().sendFeedback(() -> Components.parse(
                            RccServer.CONFIG.textFormats.commands.back.teleporting,
                            playerContext
                    ), false);
                    lastPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}

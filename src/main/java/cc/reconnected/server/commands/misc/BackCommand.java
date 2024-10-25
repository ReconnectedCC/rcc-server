package cc.reconnected.server.commands.misc;

import cc.reconnected.server.core.BackTracker;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class BackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("back")
                .requires(Permissions.require("rcc.command.back", true))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var player = context.getSource().getPlayer();

                    var lastPosition = BackTracker.lastPlayerPositions.get(player.getUuid());
                    if (lastPosition == null) {
                        context.getSource().sendFeedback(() -> Text.literal("There is no position to return back to.").formatted(Formatting.RED), false);
                        return 1;
                    }

                    context.getSource().sendFeedback(() -> Text.literal("Teleporting to previous position...").formatted(Formatting.GOLD), false);
                    lastPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}

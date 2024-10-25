package cc.reconnected.server.commands.warp;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.*;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("warp")
                .requires(Permissions.require("rcc.command.warp", true))
                .then(argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            if (!context.getSource().isExecutedByPlayer())
                                return CommandSource.suggestMatching(new String[]{}, builder);

                            var serverState = RccServer.state.getServerState();
                            return CommandSource.suggestMatching(serverState.warps.keySet().stream(), builder);
                        })
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name"))));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String name) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
            return 1;
        }
        var player = context.getSource().getPlayer();
        var serverState = RccServer.state.getServerState();
        var warps = serverState.warps;

        if (!warps.containsKey(name)) {
            context.getSource().sendFeedback(() -> Text.literal("The warp ")
                    .append(Text.literal(name).formatted(Formatting.GOLD))
                    .append(" does not exist!")
                    .formatted(Formatting.RED), false);
            return 1;
        }

        context.getSource().sendFeedback(() -> Text
                .literal("Teleporting to ")
                .append(Text.literal(name).formatted(Formatting.GREEN))
                .append("...")
                .formatted(Formatting.GOLD), false);

        var warpPosition = warps.get(name);
        warpPosition.teleport(player);

        return 1;
    }
}

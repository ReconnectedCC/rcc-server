package cc.reconnected.server.commands.home;

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

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DeleteHomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("delhome")
                .requires(Permissions.require("rcc.command.delhome", true))
                .executes(context -> execute(context, "home"))
                .then(argument("name", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            if (!context.getSource().isExecutedByPlayer())
                                return CommandSource.suggestMatching(new String[]{}, builder);

                            var playerState = RccServer.state.getPlayerState(context.getSource().getPlayer().getUuid());
                            return CommandSource.suggestMatching(playerState.homes.keySet().stream(), builder);
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
        var playerState = RccServer.state.getPlayerState(player.getUuid());
        var homes = playerState.homes;

        if (!homes.containsKey(name)) {
            context.getSource().sendFeedback(() -> Text.literal("The home ")
                    .append(Text.literal(name).formatted(Formatting.GOLD))
                    .append(" does not exist!")
                    .formatted(Formatting.RED), false);
            return 1;
        }

        homes.remove(name);
        RccServer.state.savePlayerState(player.getUuid(), playerState);

        context.getSource().sendFeedback(() -> Text
                .literal("Home ")
                .append(Text.literal(name).formatted(Formatting.GOLD))
                .append(" deleted!")
                .formatted(Formatting.GREEN), false);

        return 1;
    }
}

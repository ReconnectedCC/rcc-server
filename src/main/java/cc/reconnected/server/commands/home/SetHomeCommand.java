package cc.reconnected.server.commands.home;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.*;

public class SetHomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("sethome")
                .requires(Permissions.require("rcc.command.sethome", true))
                .executes(context -> execute(context,
                        "home",
                        false))
                .then(argument("name", StringArgumentType.word())
                        .executes(context -> execute(context,
                                StringArgumentType.getString(context, "name"),
                                false))
                        .then(argument("force", BoolArgumentType.bool())
                                .executes(context -> execute(context,
                                        StringArgumentType.getString(context, "name"),
                                        BoolArgumentType.getBool(context, "force")))));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, String name, boolean forced) {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
            return 1;
        }
        var player = context.getSource().getPlayer();
        var playerState = RccServer.state.getPlayerState(player.getUuid());

        var homes = playerState.homes;

        if (homes.containsKey(name) && !forced) {
            var text = Component.text("You already have set this home.")
                    .appendNewline().appendSpace()
                    .append(Components.makeButton(
                            Component.text("Force set home", NamedTextColor.GOLD),
                            Component.text("Click to force set the home"),
                            "/sethome " + name + " true"
                    ));

            context.getSource().sendFailure(text);

            return 1;
        }

        var homePosition = new ServerPosition(player);
        homes.put(name, homePosition);

        RccServer.state.savePlayerState(player.getUuid(), playerState);

        context.getSource().sendFeedback(() -> Text.literal("New home ")
                .append(Text.literal(name).formatted(Formatting.GOLD))
                .append(" set!")
                .formatted(Formatting.GREEN), false);

        return 1;
    }
}

package cc.reconnected.server.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GodCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("god")
                .requires(Permissions.require("rcc.command.god", 3))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }
                    var player = context.getSource().getPlayer();
                    context.getSource().sendFeedback(() -> toggleGod(player), true);

                    return 1;
                })
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var playerManager = context.getSource().getServer().getPlayerManager();
                            return CommandSource.suggestMatching(
                                    playerManager.getPlayerNames(),
                                    builder);
                        })
                        .executes(context -> {
                            var playerName = StringArgumentType.getString(context, "player");
                            var player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
                            if (player == null) {
                                context.getSource().sendFeedback(() -> Text.literal("Player not found").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                                return 1;
                            }

                            context.getSource().sendFeedback(() -> toggleGod(player), true);

                            return 1;
                        }));

        dispatcher.register(rootCommand);
    }

    private static Text toggleGod(ServerPlayerEntity player) {
        var abilities = player.getAbilities();

        abilities.invulnerable = !abilities.invulnerable;
        //player.setInvulnerable(!player.isInvulnerable());
        player.sendAbilitiesUpdate();

        return Text.literal(
                        abilities.invulnerable ?
                                "God mode enabled" :
                                "God mode disabled"
                )
                .append(" for ")
                .append(player.getDisplayName())
                .setStyle(Style.EMPTY.withColor(
                        abilities.invulnerable ?
                                Formatting.GREEN :
                                Formatting.RED
                ));
    }
}
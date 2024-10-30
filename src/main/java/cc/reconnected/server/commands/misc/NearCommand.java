package cc.reconnected.server.commands.misc;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;

import static net.minecraft.server.command.CommandManager.*;

public class NearCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("near")
                .requires(Permissions.require("rcc.command.near", 2))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }
                    return execute(context, RccServer.CONFIG.nearCommand.nearCommandDefaultRange(), context.getSource().getPlayer());
                })
                .then(argument("radius", IntegerArgumentType.integer(0, RccServer.CONFIG.nearCommand.nearCommandMaxRange()))
                        .executes(context -> {
                            if (!context.getSource().isExecutedByPlayer()) {
                                context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                                return 1;
                            }
                            return execute(context, IntegerArgumentType.getInteger(context, "radius"), context.getSource().getPlayer());
                        }));

        dispatcher.register(rootCommand);
    }

    private static int execute(CommandContext<ServerCommandSource> context, int range, ServerPlayerEntity sourcePlayer) {
        var list = new ArrayList<ClosePlayers>();

        var sourcePos = sourcePlayer.getPos();
        sourcePlayer.getServerWorld().getPlayers().forEach(targetPlayer -> {
            var targetPos = targetPlayer.getPos();
            if (!sourcePlayer.getUuid().equals(targetPlayer.getUuid()) && sourcePos.isInRange(targetPos, range)) {
                var distance = sourcePos.distanceTo(targetPos);
                list.add(new ClosePlayers(targetPlayer.getDisplayName(), distance));
            }
        });

        if(list.isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("There is no one near you.").formatted(Formatting.GOLD), false);
            return 1;
        }

        list.sort(Comparator.comparingDouble(ClosePlayers::distance));

        var text = Text.empty().append(Text.literal("Nearest players: ").formatted(Formatting.GOLD));
        var comma = Text.literal(", ").formatted(Formatting.GOLD);
        for (int i = 0; i < list.size(); i++) {
            var player = list.get(i);
            if (i > 0) {
                text = text.append(comma);
            }
            text = text.append(player.displayName)
                    .append(" ")
                    .append(Text.literal(String.format("(%.1fm)", player.distance)).formatted(Formatting.GREEN));
        }

        final var finalText = text;
        context.getSource().sendFeedback(() -> finalText, false);

        return 1;
    }

    private record ClosePlayers(Text displayName, double distance) {
    }
}

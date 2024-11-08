package cc.reconnected.server.commands.spawn;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("spawn")
                .requires(Permissions.require("rcc.command.spawn", true))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var player = context.getSource().getPlayer();
                    var serverState = RccServer.state.getServerState();
                    var playerContext = PlaceholderContext.of(player);
                    var spawnPosition = serverState.spawn;
                    if (spawnPosition == null) {
                        var server = context.getSource().getServer();
                        var spawnPos = server.getOverworld().getSpawnPos();
                        spawnPosition = new ServerPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0, server.getOverworld());
                    }

                    context.getSource().sendFeedback(() -> Components.parse(
                            RccServer.CONFIG.textFormats.commands.spawn.teleporting,
                            playerContext
                    ), false);
                    spawnPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}

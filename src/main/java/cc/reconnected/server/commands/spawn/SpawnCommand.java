package cc.reconnected.server.commands.spawn;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class SpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("spawn")
                .requires(Permissions.require("rcc.command.spawn", true))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
                        return 1;
                    }

                    var player = context.getSource().getPlayer();
                    var serverState = RccServer.state.getServerState();
                    var spawnPosition = serverState.spawn;
                    if(spawnPosition == null) {
                        var server = context.getSource().getServer();
                        var spawnPos = server.getOverworld().getSpawnPos();
                        spawnPosition = new ServerPosition(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0, server.getOverworld());
                    }

                    context.getSource().sendFeedback(() -> Text.literal("Teleporting to spawn...").formatted(Formatting.GOLD), false);
                    spawnPosition.teleport(player);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}

package cc.reconnected.server.commands.misc;

import cc.reconnected.server.core.AfkTracker;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class AfkCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("afk")
                .requires(Permissions.require("rcc.command.afk", true))
                .executes(context -> {
                    var player = context.getSource().getPlayerOrThrow();
                    AfkTracker.getInstance().setPlayerAfk(player, true);

                    return 1;
                });

        dispatcher.register(rootCommand);
    }
}

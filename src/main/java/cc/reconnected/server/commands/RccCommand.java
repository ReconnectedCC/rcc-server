package cc.reconnected.server.commands;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class RccCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var rootCommand = literal("rcc")
                .requires(Permissions.require("rcc.command.afk", 3))
                .then(literal("reload")
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("Reloading RCC config..."), true);

                            try {
                                RccServer.CONFIG.load();
                            } catch(Exception e) {
                                RccServer.LOGGER.error("Failed to load RCC config", e);
                                context.getSource().sendFeedback(() -> Text.of("Failed to load RCC config. Check console for more info."), true);
                                return 1;
                            }

                            context.getSource().sendFeedback(() -> Text.of("Reloaded RCC config"), true);

                            return 1;
                        }));

        dispatcher.register(rootCommand);
    }
}

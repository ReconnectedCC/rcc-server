package cc.reconnected.server.commands.admin;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.api.events.RccEvents;
import cc.reconnected.server.config.ConfigManager;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

import static net.minecraft.server.command.CommandManager.literal;

public class RccCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var rootCommand = literal("rcc")
                .requires(Permissions.require("rcc.command.rcc", 3))
                .then(literal("reload")
                        .requires(Permissions.require("rcc.command.rcc.reload", 3))
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("Reloading RCC config..."), true);

                            try {
                                RccServer.CONFIG = ConfigManager.load();
                            } catch(Exception e) {
                                RccServer.LOGGER.error("Failed to load RCC config", e);
                                context.getSource().sendFeedback(() -> Text.of("Failed to load RCC config. Check console for more info."), true);
                                return 1;
                            }

                            RccEvents.RELOAD.invoker().onReload(RccServer.getInstance());

                            context.getSource().sendFeedback(() -> Text.of("Reloaded RCC config"), true);

                            return 1;
                        }));

        dispatcher.register(rootCommand);
    }
}

package cc.reconnected.server.commands;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.*;

public class RccCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                literal("rcc")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            return 1;
                        })
                        .then(literal("clearcache")
                                .executes(context -> {
                                    RccServer.getInstance().playerTable().clearCache();
                                    context.getSource().sendFeedback(() -> Text.literal("RCC PlayerTable cache cleared!"), false);
                                    return 1;
                                })
                        )
        );
    }
}

package cc.reconnected.server.commands.tell;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class ReplyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var messageNode = dispatcher.register(literal("reply")
                .requires(Permissions.require("rcc.command.tell", true))
                .then(argument("message", StringArgumentType.greedyString())
                        .executes(ReplyCommand::execute)));

        dispatcher.register(literal("r").redirect(messageNode));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var senderName = source.getName();
        var message = StringArgumentType.getString(context, "message");

        if (!TellCommand.lastSender.containsKey(senderName)) {
            var playerContext = PlaceholderContext.of(context.getSource());
            source.sendFeedback(() -> Components.parse(
                    RccServer.CONFIG.textFormats.commands.tell.noLastSenderReply,
                    playerContext
            ), false);
            return 1;
        }

        var targetName = TellCommand.lastSender.get(senderName);

        TellCommand.sendDirectMessage(targetName, source, message);

        return 1;
    }
}

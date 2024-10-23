package cc.reconnected.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.*;


public class ReplyCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
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
            source.sendFeedback(() -> Text.literal("You have no one to reply to.").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            return 1;
        }

        var targetName = TellCommand.lastSender.get(senderName);

        TellCommand.sendDirectMessage(targetName, source, message);

        return 1;
    }
}

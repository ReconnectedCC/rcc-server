package cc.reconnected.server.commands.tell;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.TextParserV1;
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
            var playerContext = PlaceholderContext.of(context.getSource());
            source.sendFeedback(() -> Placeholders.parseText(TextParserV1.DEFAULT.parseNode(RccServer.CONFIG.textFormats.commands.tell.noLastSenderReply), playerContext), false);
            return 1;
        }

        var targetName = TellCommand.lastSender.get(senderName);

        TellCommand.sendDirectMessage(targetName, source, message);

        return 1;
    }
}

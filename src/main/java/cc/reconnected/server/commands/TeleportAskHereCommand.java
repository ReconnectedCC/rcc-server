package cc.reconnected.server.commands;

import cc.reconnected.server.RccServer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TeleportAskHereCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var node = dispatcher.register(literal("tpahere")
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var playerManager = context.getSource().getServer().getPlayerManager();
                            return CommandSource.suggestMatching(
                                    playerManager.getPlayerNames(),
                                    builder);
                        })
                        .executes(context -> {
                            execute(context);
                            return 1;
                        })));

        dispatcher.register(literal("tpaskhere").redirect(node));
    }

    private static void execute(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        if (!source.isExecutedByPlayer()) {
            source.sendFeedback(() -> Text.of("This command can only be executed by players!"), false);
            return;
        }

        var server = source.getServer();
        var player = source.getPlayer();
        var targetName = StringArgumentType.getString(context, "player");
        var playerManager = server.getPlayerManager();
        var target = playerManager.getPlayer(targetName);
        if (target == null) {
            source.sendFeedback(() -> Text.literal("Player \"" + targetName + "\" not found!").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
            return;
        }

        var request = new TeleportAskCommand.TeleportRequest(target.getUuid(), player.getUuid());
        var targetRequests = RccServer.teleportRequests.get(target.getUuid());
        targetRequests.addLast(request);

        var requestMessage = Component.empty()
                .append(player.getDisplayName())
                .appendSpace()
                .append(Component.text("requested you to teleport to them.", NamedTextColor.GOLD))
                .appendNewline().appendSpace()
                .append(TeleportAskCommand.makeButton(Component.text("Accept", NamedTextColor.GREEN), Component.text("Click to accept request"), "/tpaccept " + request.requestId))
                .appendSpace()
                .append(TeleportAskCommand.makeButton(Component.text("Refuse", NamedTextColor.RED), Component.text("Click to refuse request"), "/tpdeny " + request.requestId));

        target.sendMessage(requestMessage);

        source.sendFeedback(() -> Text.literal("Teleport request sent.").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
    }
}

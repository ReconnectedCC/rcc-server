package cc.reconnected.server.commands;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.parser.MarkdownParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.*;


public class TellCommand {
    public static final HashMap<String, String> lastSender = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var messageNode = dispatcher.register(literal("msg")
                .then(argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            var playerManager = context.getSource().getServer().getPlayerManager();
                            return CommandSource.suggestMatching(
                                    playerManager.getPlayerNames(),
                                    builder);
                        })
                        .then(argument("message", StringArgumentType.greedyString())
                                .executes(TellCommand::execute))));

        dispatcher.register(literal("tell").redirect(messageNode));
        dispatcher.register(literal("w").redirect(messageNode));
        dispatcher.register(literal("dm").redirect(messageNode));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        var source = context.getSource();
        var targetName = StringArgumentType.getString(context, "player");
        var message = StringArgumentType.getString(context, "message");

        sendDirectMessage(targetName, source, message);
        return 1;
    }

    public static void sendDirectMessage(String targetName, ServerCommandSource source, String message) {
        Text targetDisplayName;
        ServerPlayerEntity targetPlayer = null;
        if (targetName.equalsIgnoreCase("server")) {
            targetDisplayName = Text.of("Server");
        } else {
            targetPlayer = source.getServer().getPlayerManager().getPlayer(targetName);
            if (targetPlayer == null) {
                source.sendFeedback(() -> Text.literal("Player \"" + targetName + "\" not found").setStyle(Style.EMPTY.withColor(Formatting.RED)), false);
                return;
            }
            targetDisplayName = targetPlayer.getDisplayName();
        }

        var parsedMessage = MarkdownParser.defaultParser.parseNode(message);
        var text = MiniMessage.miniMessage().deserialize(RccServer.CONFIG.tellMessage(),
                Placeholder.component("source", source.getDisplayName()),
                Placeholder.component("target", targetDisplayName),
                Placeholder.component("message", parsedMessage.toText()));

        lastSender.put(targetName, source.getName());

        if (!source.getName().equals(targetName)) {
            source.sendMessage(text);
        }
        if(targetPlayer != null) {
            targetPlayer.sendMessage(text);
            if(source.isExecutedByPlayer()) {
                source.getServer().sendMessage(text);
            }
        } else {
            // avoid duped message
            source.getServer().sendMessage(text);
        }

        var lp = RccServer.getInstance().luckPerms();
        var playerAdapter = lp.getPlayerAdapter(ServerPlayerEntity.class);
        var spyText = MiniMessage.miniMessage().deserialize(RccServer.CONFIG.tellMessageSpy(),
                Placeholder.component("source", source.getDisplayName()),
                Placeholder.component("target", targetDisplayName),
                Placeholder.component("message", parsedMessage.toText()));
        source.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            var playerName = player.getGameProfile().getName();
            if(playerName.equals(targetName) || playerName.equals(source.getName())) {
                return;
            }
            var playerPerms = playerAdapter.getPermissionData(player);
            if(playerPerms.checkPermission("rcc.tell.spy").asBoolean()) {
                player.sendMessage(spyText);
            };
        });
    }
}

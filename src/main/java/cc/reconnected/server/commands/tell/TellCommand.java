package cc.reconnected.server.commands.tell;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.parser.MarkdownParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.PatternPlaceholderParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.Map;

import static net.minecraft.server.command.CommandManager.*;


public class TellCommand {
    public static final HashMap<String, String> lastSender = new HashMap<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var messageNode = dispatcher.register(literal("msg")
                .requires(Permissions.require("rcc.command.tell", true))
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
                var placeholders = Map.of(
                        "targetPlayer", Text.of(targetName)
                );
                var sourceContext = PlaceholderContext.of(source);

                source.sendFeedback(() -> Placeholders.parseText(
                        TextParserV1.DEFAULT.parseNode(RccServer.CONFIG.textFormats.commands.tell.playerNotFound),
                        sourceContext,
                        PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders
                ), false);
                return;
            }
            targetDisplayName = targetPlayer.getDisplayName();
        }

        var parsedMessage = MarkdownParser.defaultParser.parseNode(message).toText();

        var serverContext = PlaceholderContext.of(source.getServer());
        var sourceContext = PlaceholderContext.of(source);
        PlaceholderContext targetContext;
        if (targetPlayer == null) {
            targetContext = serverContext;
        } else {
            targetContext = PlaceholderContext.of(targetPlayer);
        }


        var you = TextParserV1.DEFAULT.parseNode(RccServer.CONFIG.textFormats.commands.tell.you).toText();

        var placeholdersToSource = Map.of(
                "sourcePlayer", you,
                "targetPlayer", targetDisplayName,
                "message", parsedMessage
        );

        var placeholdersToTarget = Map.of(
                "sourcePlayer", source.getDisplayName(),
                "targetPlayer", you,
                "message", parsedMessage
        );

        var placeholders = Map.of(
                "sourcePlayer", source.getDisplayName(),
                "targetPlayer", targetDisplayName,
                "message", parsedMessage
        );

        var parser = TextParserV1.DEFAULT;

        var sourceText = Placeholders.parseText(parser.parseNode(RccServer.CONFIG.textFormats.commands.tell.message), sourceContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholdersToSource);
        var targetText = Placeholders.parseText(parser.parseNode(RccServer.CONFIG.textFormats.commands.tell.message), targetContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholdersToTarget);
        var genericText = Placeholders.parseText(parser.parseNode(RccServer.CONFIG.textFormats.commands.tell.message), serverContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);
        var spyText = Placeholders.parseText(parser.parseNode(RccServer.CONFIG.textFormats.commands.tell.messageSpy), serverContext, PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, placeholders);

        lastSender.put(targetName, source.getName());
        lastSender.put(source.getName(), targetName);

        if (!source.getName().equals(targetName)) {
            source.sendMessage(sourceText);
        }
        if (targetPlayer != null) {
            targetPlayer.sendMessage(targetText);
            if (source.isExecutedByPlayer()) {
                source.getServer().sendMessage(genericText);
            }
        } else {
            // avoid duped message
            source.getServer().sendMessage(targetText);
        }

        var lp = RccServer.getInstance().luckPerms();
        var playerAdapter = lp.getPlayerAdapter(ServerPlayerEntity.class);
        source.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            var playerName = player.getGameProfile().getName();
            if (playerName.equals(targetName) || playerName.equals(source.getName())) {
                return;
            }
            var playerPerms = playerAdapter.getPermissionData(player);
            if (playerPerms.checkPermission("rcc.tell.spy").asBoolean()) {
                player.sendMessage(spyText);
            }
        });
    }
}

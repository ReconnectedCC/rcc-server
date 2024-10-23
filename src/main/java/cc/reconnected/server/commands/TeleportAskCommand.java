package cc.reconnected.server.commands;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.struct.ServerPosition;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.Duration;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.*;

public class TeleportAskCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        var node = dispatcher.register(literal("tpa")
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

        dispatcher.register(literal("tpask").redirect(node));
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

        var request = new TeleportRequest(player.getUuid(), target.getUuid());
        var targetRequests = RccServer.teleportRequests.get(target.getUuid());
        targetRequests.addLast(request);

        var requestMessage = Component.empty()
                .append(player.getDisplayName())
                .appendSpace()
                .append(Component.text("requested to teleport to you.", NamedTextColor.GOLD))
                .appendNewline().appendSpace()
                .append(makeButton(Component.text("Accept", NamedTextColor.GREEN), Component.text("Click to accept request"), "/tpaccept " + request.requestId))
                .appendSpace()
                .append(makeButton(Component.text("Refuse", NamedTextColor.RED), Component.text("Click to refuse request"), "/tpdeny " + request.requestId));

        target.sendMessage(requestMessage);

        source.sendFeedback(() -> Text.literal("Teleport request sent.").setStyle(Style.EMPTY.withColor(Formatting.GREEN)), false);
    }

    public static Component makeButton(ComponentLike text, ComponentLike hoverText, String command) {
        return Component.empty()
                .append(Component.text("["))
                .append(text)
                .append(Component.text("]"))
                .color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(hoverText))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public static void teleport(ServerPlayerEntity sourcePlayer, ServerPlayerEntity targetPlayer) {
        RccServer.lastPlayerPositions.put(sourcePlayer.getUuid(), new ServerPosition(sourcePlayer));
        sourcePlayer.teleport(
                targetPlayer.getServerWorld(),
                targetPlayer.getX(),
                targetPlayer.getY(),
                targetPlayer.getZ(),
                targetPlayer.getYaw(),
                targetPlayer.getPitch()
        );
    }

    public static class TeleportRequest {
        public UUID requestId = UUID.randomUUID();
        public UUID player;
        public UUID target;
        public int remainingTicks;

        public TeleportRequest(UUID player, UUID target) {
            this.player = player;
            this.target = target;
            // Seconds in config per 20 ticks
            this.remainingTicks = RccServer.CONFIG.teleportRequestTimeout() * 20;
        }

        public void expire() {
            remainingTicks = 0;
        }
    }

}

package cc.reconnected.server.core;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.api.events.BossBarEvents;
import cc.reconnected.server.util.Components;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BossBarManager {
    private static BossBarManager instance;

    public static BossBarManager getInstance() {
        return instance;
    }

    private static MinecraftServer server;
    private static ConcurrentLinkedDeque<TimeBar> timeBars = new ConcurrentLinkedDeque<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static void register() {
        instance = new BossBarManager();

        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // every second
            if (server.getTicks() % 20 == 0) {
                for (var timeBar : timeBars) {
                    var remove = timeBar.elapse();
                    BossBarEvents.PROGRESS.invoker().onProgress(timeBar, server);

                    var players = server.getPlayerManager().getPlayerList();
                    showBar(players, timeBar);

                    if (remove) {
                        timeBars.remove(timeBar);
                        BossBarEvents.END.invoker().onEnd(timeBar, server);
                        hideBar(players, timeBar);
                    }
                }
            }
        });
    }

    private static void showBar(Collection<ServerPlayerEntity> players, TimeBar timeBar) {
        timeBar.getBossBar().addPlayers(players);
    }

    private static void hideBar(Collection<ServerPlayerEntity> players, TimeBar timeBar) {
        players.forEach(player -> {
            timeBar.getBossBar().removePlayer(player);

        });
    }

    public TimeBar startTimeBar(String label, int seconds, BossBar.Color color, BossBar.Style style, boolean countdown) {
        var countdownBar = new TimeBar(label, seconds, countdown, color, style);

        timeBars.add(countdownBar);

        var players = server.getPlayerManager().getPlayerList();
        showBar(players, countdownBar);

        BossBarEvents.START.invoker().onStart(countdownBar, server);

        return countdownBar;
    }

    public boolean cancelTimeBar(TimeBar timeBar) {
        var success = timeBars.remove(timeBar);
        if (success) {
            var players = server.getPlayerManager().getPlayerList();
            hideBar(players, timeBar);
            BossBarEvents.CANCEL.invoker().onCancel(timeBar, server);
        }
        return success;
    }

    public boolean cancelTimeBar(UUID uuid) {
        var progressBar = timeBars.stream().filter(p -> p.uuid.equals(uuid)).findFirst().orElse(null);
        if (progressBar == null) {
            return false;
        }

        return cancelTimeBar(progressBar);
    }

    public static class TimeBar {
        private final UUID uuid = UUID.randomUUID();
        private final CommandBossBar bossBar;
        private final String label;
        private final int time;
        private int elapsedSeconds = 0;
        private final boolean countdown;

        public TimeBar(String label, int time, boolean countdown, BossBar.Color color, BossBar.Style style) {
            this.bossBar = new CommandBossBar(Identifier.of(RccServer.MOD_ID, uuid.toString()), Text.of(label));
            this.bossBar.setColor(color);
            this.bossBar.setStyle(style);
            this.label = label;
            this.time = time;
            this.countdown = countdown;
            updateName();
            updateProgress();
        }

        public static String formatTime(int totalSeconds) {
            var hours = totalSeconds / 3600;
            var minutes = (totalSeconds / 60) % 60;
            var seconds = totalSeconds % 60;
            if(totalSeconds >= 3600) {
                return String.format("%dh%dm%ds", hours, minutes, seconds);
            }
            return String.format("%dm%ds", minutes, seconds);
        }

        public void updateName() {
            var totalTime = formatTime(this.time);
            var elapsedTime = formatTime(this.elapsedSeconds);

            var remaining = time - elapsedSeconds;
            var remainingTime = formatTime(remaining);
            var text = miniMessage.deserialize(label, TagResolver.resolver(
                    Placeholder.parsed("total_time", totalTime),
                    Placeholder.parsed("elapsed_time", elapsedTime),
                    Placeholder.parsed("remaining_time", remainingTime)
            ));

            bossBar.setName(Components.toText(text));
        }

        public UUID getUuid() {
            return uuid;
        }

        public CommandBossBar getBossBar() {
            return bossBar;
        }

        public String getLabel() {
            return label;
        }

        public int getTime() {
            return time;
        }

        public int getElapsedSeconds() {
            return elapsedSeconds;
        }

        public boolean isCountdown() {
            return countdown;
        }

        public boolean elapse() {
            this.elapsedSeconds++;

            updateProgress();
            updateName();

            return this.elapsedSeconds >= this.time;
        }

        private void updateProgress() {
            float progress = (float) elapsedSeconds / (float) time;
            if (countdown) {
                progress = 1f - progress;
            }

            bossBar.setPercent(Math.min(
                    Math.max(
                            progress,
                            0f),
                    1f));
        }
    }
}

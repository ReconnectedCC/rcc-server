package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import eu.pb4.placeholders.api.PlaceholderContext;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;

public class CustomDeathMessage {
    public static Text onDeath(ServerPlayerEntity player, DamageTracker instance) {
        var deathMessage = instance.getDeathMessage();
        var playerContext = PlaceholderContext.of(player);

        return Components.parse(
                RccServer.CONFIG.textFormats.deathFormat,
                playerContext,
                Map.of("message", deathMessage)
        );
    }
}

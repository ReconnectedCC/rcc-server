package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.util.Components;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CustomDeathMessage {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Text onDeath(ServerPlayerEntity player, DamageTracker instance) {
        var deathMessage = instance.getDeathMessage();
        var deathComponent = miniMessage.deserialize(RccServer.CONFIG.customChatFormat.deathFormat(),
                TagResolver.resolver(
                        Placeholder.component("death_message", deathMessage)
                ));

        return Components.toText(deathComponent);
    }
}

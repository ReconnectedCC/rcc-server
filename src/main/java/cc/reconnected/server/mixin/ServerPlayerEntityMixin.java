package cc.reconnected.server.mixin;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.customChat.CustomDeathMessage;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Unique
    private static final NodeParser parser = NodeParser.merge(TextParserV1.DEFAULT, Placeholders.DEFAULT_PLACEHOLDER_PARSER);

    @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
    private void rccServer$customizePlayerListName(CallbackInfoReturnable<Text> callback) {
        if (RccServer.CONFIG.customTabList.enableTabList) {
            var player = (ServerPlayerEntity) (Object) this;
            var playerContext = PlaceholderContext.of(player);
            var text = Placeholders.parseText(parser.parseNode(RccServer.CONFIG.customTabList.playerTabName), playerContext);
            callback.setReturnValue(text);
        }
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;"))
    private Text rccServer$getDeathMessage(DamageTracker instance) {
        var player = (ServerPlayerEntity) (Object) this;
        return CustomDeathMessage.onDeath(player, instance);
    }
}

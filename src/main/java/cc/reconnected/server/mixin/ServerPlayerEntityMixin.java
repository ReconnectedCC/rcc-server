package cc.reconnected.server.mixin;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.core.BackTracker;
import cc.reconnected.server.core.customChat.CustomDeathMessage;
import cc.reconnected.server.struct.ServerPosition;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

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

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
    public void rccServer$requestTeleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir) {
        var player = (ServerPlayerEntity) (Object) this;
        BackTracker.lastPlayerPositions.put(player.getUuid(), new ServerPosition(player));
    }
}

package cc.reconnected.server.mixin;

import cc.reconnected.server.core.customChat.CustomConnectionMessage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Unique
    private ServerPlayerEntity rccServer$player = null;

    @Inject(method="onPlayerConnect", at = @At("HEAD"))
    private void rccServer$onJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        rccServer$player = player;
    }

    @Inject(method="onPlayerConnect", at = @At("RETURN"))
    private void rccServer$onJoinReturn(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        rccServer$player = null;
    }

    @ModifyArg(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public Text rccServer$getPlayerJoinMessage(Text message) {
        var ogText = (TranslatableTextContent) message.getContent();
        var args = ogText.getArgs();

        if (args.length == 1) {
            return CustomConnectionMessage.onJoin(rccServer$player);
        } else {
            return CustomConnectionMessage.onJoinRenamed(rccServer$player, (String) args[1]);
        }
    }
}

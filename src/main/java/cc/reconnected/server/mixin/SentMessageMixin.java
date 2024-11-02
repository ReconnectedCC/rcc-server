package cc.reconnected.server.mixin;

import cc.reconnected.server.struct.CustomChatMessage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SentMessage.class)
public interface SentMessageMixin {

    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void rccServer$of(SignedMessage message, CallbackInfoReturnable<SentMessage> cir) {

        if(message.hasSignature()) {
            cir.setReturnValue(new CustomChatMessage(message));
        } else {
            cir.setReturnValue(new SentMessage.Profileless(message.getContent()));
        }
    }
}

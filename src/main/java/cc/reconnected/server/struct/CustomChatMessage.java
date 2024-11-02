package cc.reconnected.server.struct;

import cc.reconnected.server.RccServer;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record CustomChatMessage(SignedMessage message) implements SentMessage {
    @Override
    public Text getContent() {
        return this.message.getContent();
    }

    @Override
    public void send(ServerPlayerEntity sender, boolean filterMaskEnabled, MessageType.Parameters params) {
        sender.networkHandler.sendChatMessage(this.message, MessageType.params(RccServer.CHAT_TYPE, sender.server.getRegistryManager(), sender.getDisplayName()));
        //sender.networkHandler.sendChatMessage(this.message, params);
    }
}

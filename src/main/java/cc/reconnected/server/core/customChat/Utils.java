package cc.reconnected.server.core.customChat;

import cc.reconnected.server.RccServer;
import cc.reconnected.server.parser.MarkdownParser;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Utils {
    public static Text formatChatMessage(SignedMessage message, ServerPlayerEntity player) {
        var luckperms = RccServer.getInstance().luckPerms();

        var permissions = luckperms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player);
        var allowAdvancedChatFormat = permissions.checkPermission("rcc.chat.advanced").asBoolean();
        var enableMarkdown = RccServer.CONFIG.textFormats.enableChatMarkdown;

        if(!allowAdvancedChatFormat && !enableMarkdown) {
            return message.getContent();
        }

        NodeParser parser;
        if(allowAdvancedChatFormat) {
            parser = NodeParser.merge(TextParserV1.DEFAULT, MarkdownParser.defaultParser);
        } else {
            parser = MarkdownParser.defaultParser;
        }

        return parser.parseNode(message.getSignedContent()).toText();
    }
}

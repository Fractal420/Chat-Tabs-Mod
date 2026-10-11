package com.example.tibiachat.chat;

import com.example.tibiachat.TibiaChatTabsClient;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PrivateChatRenderer {
    private PrivateChatRenderer() {}

    public static boolean draw(GuiGraphicsExtractor graphics, Font font, boolean focused) {
        if (!TibiaChatTabsClient.CHAT.viewingPrivate()) {
            return false;
        }

        Conversation conv = TibiaChatTabsClient.CHAT.selectedConversation();
        if (conv == null) {
            return true;
        }

        List<ChatMessage> messages = conv.messages();
        if (messages.isEmpty()) {
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        Font drawFont = font != null ? font : mc.font;
        int screenH = mc.getWindow().getGuiScaledHeight();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int lineHeight = 10;
        int maxLines = focused ? 20 : 10;
        int scroll = Math.max(0, conv.scroll());

        int end = messages.size() - scroll;
        if (end < 1) {
            end = 1;
        }
        if (end > messages.size()) {
            end = messages.size();
        }
        int start = Math.max(0, end - maxLines);

        int baseY = screenH - 48;
        int chatWidth = Math.min(320, screenW - 8);

        int y = 0;
        for (int i = end - 1; i >= start; i--) {
            ChatMessage msg = messages.get(i);
            int drawY = baseY - (y + 1) * lineHeight;
            graphics.fill(2, drawY - 1, 2 + chatWidth, drawY + lineHeight - 1, 0x80000000);
            graphics.text(drawFont, msg.component(), 4, drawY, 0xFFFFFF, true);
            y++;
        }
        return true;
    }
}

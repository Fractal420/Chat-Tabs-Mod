package com.example.tibiachat;

import com.example.tibiachat.chat.ChatManager;
import com.example.tibiachat.config.TibiaChatConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TibiaChatTabsClient implements ClientModInitializer {
    public static final String MOD_ID = "tibia_chat_tabs";
    public static final TibiaChatConfig CONFIG = TibiaChatConfig.load();
    public static final ChatManager CHAT = new ChatManager();

    @Override
    public void onInitializeClient() {
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            String key = CHAT.classifyAndStore(message, sender, timestamp);
            return key != null && key.equals(CHAT.selectedKey());
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true;
            String key = CHAT.classifyAndStore(message, null, java.time.Instant.now());
            return key != null && key.equals(CHAT.selectedKey());
        });

        ClientSendMessageEvents.COMMAND.register(CHAT::onOutgoingCommand);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> CONFIG.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> CHAT.tick());

        HudRenderCallback.EVENT.register(this::renderUnreadBadge);
    }

    private void renderUnreadBadge(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        int unread = CHAT.totalUnread();
        if (unread <= 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        String label = "\u2709 " + unread;
        int textWidth = mc.textRenderer.getWidth(label);

        int x = 6;
        int y = mc.getWindow().getScaledHeight() - 66;

        ctx.fill(x - 3, y - 2, x + textWidth + 3, y + 10, 0x90000000);
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(label), x, y, 0xFFFFD24A);
    }
}
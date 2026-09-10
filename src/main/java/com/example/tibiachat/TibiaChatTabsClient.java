package com.example.tibiachat;

import com.example.tibiachat.chat.ChatManager;
import com.example.tibiachat.config.TibiaChatConfig;
import com.example.tibiachat.gui.TibiaChatConfigScreen;
import com.example.tibiachat.hud.HudLayout;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class TibiaChatTabsClient implements ClientModInitializer {
    public static final String MOD_ID = "tibia_chat_tabs";
    public static final TibiaChatConfig CONFIG = TibiaChatConfig.load();
    public static final ChatManager CHAT = new ChatManager();

    private static final KeyMapping.Category CHAT_TABS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "key_category"));

    private static KeyMapping openSettingsKey;

    @Override
    public void onInitializeClient() {
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.chat_tabs.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CHAT_TABS_CATEGORY
        ));

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            String key = CHAT.classifyAndStore(message, sender, timestamp);
            return key != null && key.equals(CHAT.selectedKey());
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) {
                return true;
            }

            String key = CHAT.classifyAndStore(message, null, java.time.Instant.now());
            return key != null && key.equals(CHAT.selectedKey());
        });

        ClientSendMessageEvents.COMMAND.register(CHAT::onOutgoingCommand);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> CONFIG.save());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CHAT.tick();

            while (openSettingsKey.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new TibiaChatConfigScreen(null));
                }
            }
        });

        HudRenderCallback.EVENT.register(this::renderUnreadBadge);
    }

    private void renderUnreadBadge(GuiGraphics ctx, DeltaTracker tickCounter) {
        int unread = CHAT.totalUnread();

        if (unread <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        String label = "\u2709 " + unread;
        int textWidth = mc.font.width(label);

        int x = HudLayout.notifIconX();
        int y = HudLayout.notifIconY(mc.getWindow().getGuiScaledHeight());
        float scale = HudLayout.notifIconScale();

        ctx.pose().pushMatrix();
        ctx.pose().translate(x, y);
        ctx.pose().scale(scale, scale);

        ctx.fill(-3, -2, textWidth + 3, 10, 0x90000000);
        ctx.drawString(mc.font, Component.literal(label), 0, 0, 0xFFFFD24A);

        ctx.pose().popMatrix();
    }
}
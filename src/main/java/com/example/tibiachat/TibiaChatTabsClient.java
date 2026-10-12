package com.example.tibiachat;

import com.example.tibiachat.chat.ChatManager;
import com.example.tibiachat.chat.ChatPersistence;
import com.example.tibiachat.chat.SentMessageHistory;
import com.example.tibiachat.config.TibiaChatConfig;
import com.example.tibiachat.gui.TibiaChatConfigScreen;
import com.example.tibiachat.hud.HudLayout;
import com.example.tibiachat.hud.UnreadNotifier;
import com.example.tibiachat.chat.Conversation;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#if MC >= 260100
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//#else
//$$ import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//#endif
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
//#if MC >= 260100
import net.minecraft.client.gui.GuiGraphicsExtractor;
//#else
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif
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
        //#if MC >= 260100
        openSettingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
//#else
//$$         openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
//#endif
                "key.chat_tabs.open_settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CHAT_TABS_CATEGORY
        ));

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, timestamp) -> {
            CHAT.observe(message, sender, timestamp);
            return true;
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!overlay) {
                CHAT.observe(message, null, java.time.Instant.now());
            }
            return true;
        });

        ClientSendMessageEvents.COMMAND.register(CHAT::onOutgoingCommand);

        SentMessageHistory.load();
        ChatPersistence.load();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            SentMessageHistory.applyToChat();
            ChatPersistence.applyToHud();
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                ChatPersistence.load();
                ChatPersistence.applyToHud();
                SentMessageHistory.applyToChat();
            });
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            CONFIG.save();
            SentMessageHistory.save();
            ChatPersistence.save();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CHAT.tick();

            while (openSettingsKey.consumeClick()) {
                //#if MC >= 260200
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new TibiaChatConfigScreen(null));
                }
                //#else
                //$$ if (client.screen == null) {
                //$$     client.setScreen(new TibiaChatConfigScreen(null));
                //$$ }
                //#endif
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "unread_badge"),
                this::renderUnreadBadge
        );
    }

    //#if MC >= 260100
    private void renderUnreadBadge(GuiGraphicsExtractor ctx, DeltaTracker tickCounter) {
//#else
//$$     private void renderUnreadBadge(GuiGraphics ctx, DeltaTracker tickCounter) {
//#endif
        int unread = CHAT.totalUnread();
        UnreadNotifier.clearEntries();

        if (unread <= 0) {
            UnreadNotifier.collapse();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        float scale = HudLayout.notifIconScale();
        int baseX = HudLayout.notifIconX();
        int baseY = HudLayout.notifIconY(mc.getWindow().getGuiScaledHeight());

        String badge = "\u2709 " + unread;
        int badgeW = mc.font.width(badge);
        int rowH = 12;

        UnreadNotifier.addEntry(
                "__badge__",
                baseX,
                baseY,
                baseX + Math.round((badgeW + 6) * scale),
                baseY + Math.round(rowH * scale)
        );

        if (UnreadNotifier.isExpanded()) {
            java.util.List<Conversation> list = UnreadNotifier.unreadConversations();
            int panelW = 0;
            for (Conversation c : list) {
                String line = c.playerName() + "  " + c.unread();
                panelW = Math.max(panelW, mc.font.width(line));
            }
            panelW = Math.max(panelW, badgeW) + 10;
            int panelH = list.size() * rowH + 4;
            int panelX = baseX;
            int panelY = baseY - Math.round((panelH + 2) * scale);

            ctx.pose().pushMatrix();
            ctx.pose().translate(panelX, panelY);
            ctx.pose().scale(scale, scale);
            ctx.fill(-2, -2, panelW + 2, panelH + 2, 0xE0101010);
            ctx.fill(-1, -1, panelW + 1, panelH + 1, 0xE0222222);

            int ly = 2;
            for (Conversation c : list) {
                String line = c.playerName() + "  " + c.unread();
                ctx.fill(0, ly - 1, panelW, ly + rowH - 2, 0x803A6EA5);
                //#if MC >= 260100
                ctx.text(mc.font, Component.literal(line), 4, ly, 0xFFFFD24A, true);
                //#else
                //$$ ctx.drawString(mc.font, Component.literal(line), 4, ly, 0xFFFFD24A, true);
                //#endif
                int absY0 = panelY + Math.round(ly * scale);
                int absY1 = panelY + Math.round((ly + rowH) * scale);
                UnreadNotifier.addEntry(
                        c.key(),
                        panelX,
                        absY0,
                        panelX + Math.round(panelW * scale),
                        absY1
                );
                ly += rowH;
            }
            ctx.pose().popMatrix();
        }

        ctx.pose().pushMatrix();
        ctx.pose().translate(baseX, baseY);
        ctx.pose().scale(scale, scale);
        ctx.fill(-3, -2, badgeW + 3, 10, 0x90000000);
        //#if MC >= 260100
        ctx.text(mc.font, Component.literal(badge), 0, 0, 0xFFFFD24A, true);
        //#else
        //$$ ctx.drawString(mc.font, Component.literal(badge), 0, 0, 0xFFFFD24A, true);
        //#endif
        ctx.pose().popMatrix();
    }
}

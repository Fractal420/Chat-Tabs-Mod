package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.Conversation;
import com.example.tibiachat.chat.ConversationManager;
import com.example.tibiachat.hud.HudLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenTabBarMixin {

    @Shadow
    protected EditBox input;

    private static final int TAB_W_MAIN_BASE = 50;
    private static final int TAB_W_MIN_BASE = 60;
    private static final int TAB_W_MAX_BASE = 120;
    private static final int TAB_SCROLL_STEP = 40;
    private static final int CLOSE_SIZE = 8;

    private int tibiaChatTabs$tabScroll = 0;

    private int tibiaChatTabs$tabMainWidth() {
        return Math.round(TAB_W_MAIN_BASE * HudLayout.tabTextScale());
    }

    private int tibiaChatTabs$tabWidth(Minecraft mc, String label) {
        float scale = HudLayout.tabTextScale();
        int baseW = mc.font.width(label) + 24;
        int clampedBase = Math.max(TAB_W_MIN_BASE, Math.min(TAB_W_MAX_BASE, baseW));
        return Math.round(clampedBase * scale);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void tibiaChatTabs$drawTabs(
            GuiGraphics ctx,
            int mouseX,
            int mouseY,
            float delta,
            CallbackInfo ci
    ) {
        Minecraft mc = Minecraft.getInstance();

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);
        int top = HudLayout.tabBarTop(screenH);
        int bottom = HudLayout.tabBarBottom(screenH);

        ctx.fill(left, top, right, bottom, 0xB0101010);

        int mainW = tibiaChatTabs$tabMainWidth();
        int x = left + 2;

        x = tibiaChatTabs$drawTab(
                ctx,
                mc,
                "Main",
                ConversationManager.MAIN,
                x,
                mainW,
                top,
                bottom,
                mouseX,
                mouseY,
                0
        );

        int tabsLeft = x;
        int tabsRight = right - 4;

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

        int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);

        if (tabsRight > tabsLeft) {
            ctx.enableScissor(tabsLeft, top, tabsRight, bottom);

            int tabX = tabsLeft - tibiaChatTabs$tabScroll;

            for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
                int w = tibiaChatTabs$tabWidth(mc, c.playerName());

                if (tabX + w > tabsLeft && tabX < tabsRight) {
                    tibiaChatTabs$drawTab(
                            ctx,
                            mc,
                            c.playerName(),
                            c.key(),
                            tabX,
                            w,
                            top,
                            bottom,
                            mouseX,
                            mouseY,
                            c.unread()
                    );
                }

                tabX += w;
            }

            ctx.disableScissor();
        }

        if (tibiaChatTabs$tabScroll > 0) {
            ctx.fill(tabsLeft, top, tabsLeft + 8, bottom, 0xCC101010);
            ctx.drawString(mc.font, Component.literal("‹"), tabsLeft + 1, top + 3, 0xFFFFFFFF);
        }

        if (tibiaChatTabs$tabScroll < maxScroll) {
            ctx.fill(tabsRight - 8, top, tabsRight, bottom, 0xCC101010);
            ctx.drawString(mc.font, Component.literal("›"), tabsRight - 6, top + 3, 0xFFFFFFFF);
        }
    }

    private int tibiaChatTabs$totalTabsWidth(Minecraft mc) {
        int width = 0;
        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            width += tibiaChatTabs$tabWidth(mc, c.playerName());
        }
        return width;
    }

    private int tibiaChatTabs$maxTabScroll(Minecraft mc, int tabsLeft, int tabsRight) {
        int availableWidth = Math.max(0, tabsRight - tabsLeft);
        int totalWidth = tibiaChatTabs$totalTabsWidth(mc);
        return Math.max(0, totalWidth - availableWidth);
    }

    private void tibiaChatTabs$clampTabScroll(Minecraft mc, int tabsLeft, int tabsRight) {
        int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);
        if (tibiaChatTabs$tabScroll < 0) {
            tibiaChatTabs$tabScroll = 0;
        }
        if (tibiaChatTabs$tabScroll > maxScroll) {
            tibiaChatTabs$tabScroll = maxScroll;
        }
    }

    private int tibiaChatTabs$drawTab(
            GuiGraphics ctx,
            Minecraft mc,
            String label,
            String key,
            int x,
            int w,
            int top,
            int bottom,
            int mouseX,
            int mouseY,
            int unread
    ) {
        boolean selected = TibiaChatTabsClient.CHAT.selectedKey().equals(key);
        boolean hover = mouseX >= x && mouseX < x + w && mouseY >= top && mouseY < bottom;

        ctx.fill(
                x,
                top,
                x + w - 1,
                bottom,
                selected ? 0xFF3A3A3A : (hover ? 0xFF303030 : 0xFF202020)
        );

        String shown = label;
        if (unread > 0) {
            shown = shown + " (" + unread + ")";
        }

        float textScale = HudLayout.tabTextScale();
        int closePadding = hover && !ConversationManager.MAIN.equals(key) ? Math.round((CLOSE_SIZE + 6) * textScale) : Math.round(5 * textScale);
        int maxTextWidth = w - closePadding - Math.round(4 * textScale);

        if (maxTextWidth > 0 && Math.round(mc.font.width(shown) * textScale) > maxTextWidth) {
            shown = mc.font.plainSubstrByWidth(shown, Math.max(1, Math.round((maxTextWidth - Math.round(8 * textScale)) / textScale))) + "…";
        }

        ctx.pose().pushMatrix();
        ctx.pose().translate(x + Math.round(5 * textScale), top + 4);
        ctx.pose().scale(textScale, textScale);

        ctx.drawString(
                mc.font,
                Component.literal(shown),
                0,
                0,
                unread > 0 ? 0xFFFFD24A : 0xFFFFFFFF
        );

        ctx.pose().popMatrix();

        if (hover && !ConversationManager.MAIN.equals(key)) {
            int closeX = x + w - Math.round((CLOSE_SIZE + 2) * textScale);
            int closeY = top + 4;

            ctx.drawString(
                    mc.font,
                    Component.literal("×"),
                    closeX,
                    closeY - 1,
                    0xFFFFFFFF
            );
        }

        return x + w;
    }

    private boolean tibiaChatTabs$isCloseHovered(
            double mouseX,
            double mouseY,
            int tabX,
            int tabW,
            int top,
            int bottom
    ) {
        float textScale = HudLayout.tabTextScale();
        int closeX = tabX + tabW - Math.round((CLOSE_SIZE + 2) * textScale);
        int closeY = top + 2;

        return mouseX >= closeX &&
                mouseX < closeX + Math.round(CLOSE_SIZE * textScale) &&
                mouseY >= closeY &&
                mouseY < bottom - 1;
    }

    @Inject(
            method = "mouseScrolled",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void tibiaChatTabs$onMouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Minecraft mc = Minecraft.getInstance();

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);
        int top = HudLayout.tabBarTop(screenH);
        int bottom = HudLayout.tabBarBottom(screenH);

        if (mouseX < left || mouseX >= right || mouseY < top || mouseY >= bottom) {
            return;
        }

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - 4;

        int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);
        if (maxScroll <= 0) {
            return;
        }

        double amount = Math.abs(horizontalAmount) > Math.abs(verticalAmount) ? horizontalAmount : verticalAmount;
        if (amount == 0) {
            return;
        }

        tibiaChatTabs$tabScroll -= (int) Math.round(amount * TAB_SCROLL_STEP);
        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

        cir.setReturnValue(true);
    }

    @Inject(
            method = "mouseClicked",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void tibiaChatTabs$onClick(
            MouseButtonEvent click,
            boolean doubled,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);
        int top = HudLayout.tabBarTop(screenH);
        int bottom = HudLayout.tabBarBottom(screenH);

        if (click.x() < left || click.x() >= right || click.y() < top || click.y() >= bottom) {
            return;
        }

        int mainW = tibiaChatTabs$tabMainWidth();
        int x = left + 2;

        if (click.x() >= x && click.x() < x + mainW) {
            tibiaChatTabs$select(ConversationManager.MAIN);
            cir.setReturnValue(true);
            return;
        }

        int tabsLeft = x + mainW;
        int tabsRight = right - 4;

        if (click.x() < tabsLeft || click.x() > tabsRight) {
            return;
        }

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

        int tabX = tabsLeft - tibiaChatTabs$tabScroll;

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());

            if (tabX + w <= tabsLeft) {
                tabX += w;
                continue;
            }

            if (tabX >= tabsRight) {
                break;
            }

            if (click.x() >= tabX && click.x() < tabX + w) {
                if (tibiaChatTabs$isCloseHovered(click.x(), click.y(), tabX, w, top, bottom)) {
                    tibiaChatTabs$closeTab(c.key());
                } else {
                    tibiaChatTabs$select(c.key());
                }
                cir.setReturnValue(true);
                return;
            }

            tabX += w;
        }
    }

    private void tibiaChatTabs$closeTab(String key) {
        String selectedKey = TibiaChatTabsClient.CHAT.selectedKey();
        boolean selected = key.equals(selectedKey);

        TibiaChatTabsClient.CHAT.conversations().remove(key);

        if (!selected) {
            tibiaChatTabs$clampTabScrollAfterClose();
            return;
        }

        String nextKey = ConversationManager.MAIN;
        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            nextKey = c.key();
            break;
        }

        tibiaChatTabs$select(nextKey);
    }

    private void tibiaChatTabs$clampTabScrollAfterClose() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - 4;

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);
    }

    private void tibiaChatTabs$select(String key) {
        if (TibiaChatTabsClient.CHAT.selectedKey().equals(key)) {
            return;
        }

        TibiaChatTabsClient.CHAT.select(key);

        ChatComponent hud = Minecraft.getInstance().gui.getChat();
        hud.clearMessages(false);

        for (var msg : TibiaChatTabsClient.CHAT.selectedMessages()) {
            hud.addMessage(msg.component());
        }

        tibiaChatTabs$scrollSelectedIntoView();
    }

    private void tibiaChatTabs$scrollSelectedIntoView() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - 4;

        String selectedKey = TibiaChatTabsClient.CHAT.selectedKey();

        if (ConversationManager.MAIN.equals(selectedKey)) {
            tibiaChatTabs$tabScroll = 0;
            return;
        }

        int tabX = tabsLeft;

        for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
            int w = tibiaChatTabs$tabWidth(mc, c.playerName());

            if (c.key().equals(selectedKey)) {
                int visibleLeft = tabX - tibiaChatTabs$tabScroll;
                int visibleRight = visibleLeft + w;

                if (visibleLeft < tabsLeft) {
                    tibiaChatTabs$tabScroll -= tabsLeft - visibleLeft;
                } else if (visibleRight > tabsRight) {
                    tibiaChatTabs$tabScroll += visibleRight - tabsRight;
                }

                tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);
                return;
            }

            tabX += w;
        }
    }

    @Inject(
            method = "keyPressed",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void tibiaChatTabs$onKey(
            KeyEvent input,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (input.key() != GLFW.GLFW_KEY_ENTER && input.key() != GLFW.GLFW_KEY_KP_ENTER) {
            return;
        }

        String key = TibiaChatTabsClient.CHAT.selectedKey();
        if (ConversationManager.MAIN.equals(key)) {
            return;
        }

        Conversation c = TibiaChatTabsClient.CHAT.conversations().getByKey(key);
        if (c == null || this.input == null) {
            return;
        }

        String text = this.input.getValue().trim();
        if (text.isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        String whisperCmd = TibiaChatTabsClient.CONFIG.whisperCommand();
        String body = whisperCmd + " " + c.playerName() + " " + text;
        String command = body.startsWith("/") ? body.substring(1) : body;

        mc.player.connection.sendCommand(command);
        this.input.setValue("");

        cir.setReturnValue(true);
    }
}

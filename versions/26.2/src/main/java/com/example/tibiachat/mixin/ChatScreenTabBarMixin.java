package com.example.tibiachat.mixin;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.Conversation;
import com.example.tibiachat.chat.ConversationManager;
import com.example.tibiachat.hud.HudLayout;
import com.example.tibiachat.gui.TibiaChatConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.List;
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
    private static final int DRAG_THRESHOLD = 4;

    private int tibiaChatTabs$tabScroll = 0;

    private String tibiaChatTabs$dragKey = null;
    private boolean tibiaChatTabs$dragActive = false;
    private double tibiaChatTabs$dragPressX;
    private double tibiaChatTabs$dragPressY;
    private int tibiaChatTabs$dragPointerOffsetX;

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
            GuiGraphicsExtractor ctx,
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

        tibiaChatTabs$tickDrag(mc, mouseX, mouseY, left, right, top, bottom);

        int barAlpha = TibiaChatTabsClient.CONFIG.tabBarAlpha();
        int barRgb = TibiaChatTabsClient.CONFIG.tabBarColor();
        ctx.fill(left, top, right, bottom, (barAlpha << 24) | barRgb);

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
        int tabsRight = right - HudLayout.settingsBtnW() - 4;

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

        int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);

        if (tabsRight > tabsLeft) {
            ctx.enableScissor(tabsLeft, top, tabsRight, bottom);

            int tabX = tabsLeft - tibiaChatTabs$tabScroll;

            Conversation dragged = null;
            int draggedW = 0;

            for (Conversation c : TibiaChatTabsClient.CHAT.conversations().all()) {
                int w = tibiaChatTabs$tabWidth(mc, c.playerName());

                boolean isDragged = tibiaChatTabs$dragActive && c.key().equals(tibiaChatTabs$dragKey);

                if (isDragged) {
                    dragged = c;
                    draggedW = w;
                } else if (tabX + w > tabsLeft && tabX < tabsRight) {
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

            if (dragged != null) {
                int floatX = (int) Math.round(mouseX - tibiaChatTabs$dragPointerOffsetX);
                floatX = Math.max(tabsLeft, Math.min(tabsRight - draggedW, floatX));

                tibiaChatTabs$drawTab(
                        ctx,
                        mc,
                        dragged.playerName(),
                        dragged.key(),
                        floatX,
                        draggedW,
                        top,
                        bottom,
                        mouseX,
                        mouseY,
                        dragged.unread()
                );
            }

            ctx.disableScissor();
        }

        if (tibiaChatTabs$tabScroll > 0) {
            ctx.fill(tabsLeft, top, tabsLeft + 8, bottom, 0xCC101010);
            ctx.text(mc.font, Component.literal("‹"), tabsLeft + 1, top + 3, 0xFFFFFFFF, true);
        }

        if (tibiaChatTabs$tabScroll < maxScroll) {
            ctx.fill(tabsRight - 8, top, tabsRight, bottom, 0xCC101010);
            ctx.text(mc.font, Component.literal("›"), tabsRight - 6, top + 3, 0xFFFFFFFF, true);
        }

        int sbX = HudLayout.settingsBtnX(screenW);
        int sbY = HudLayout.settingsBtnY(screenH);
        int sbW = HudLayout.settingsBtnW();
        int sbH = HudLayout.settingsBtnH();
        boolean sbHover = mouseX >= sbX && mouseX < sbX + sbW && mouseY >= sbY && mouseY < sbY + sbH;
        ctx.fill(sbX, sbY, sbX + sbW, sbY + sbH, sbHover ? 0xFF3A6EA5 : 0xCC202020);
        ctx.text(mc.font, Component.literal("⚙"), sbX + 3, sbY + 3, 0xFFFFFFFF, true);

        long remainingMs = TibiaChatTabsClient.CHAT.privateCooldownRemainingMs();
        if (TibiaChatTabsClient.CHAT.viewingPrivate() && remainingMs > 0) {
            double secs = remainingMs / 1000.0;
            String label = String.format(java.util.Locale.ROOT, "Cooldown: %.1fs", secs);
            int labelW = mc.font.width(label);
            int lx = 4;
            int ly = screenH - 28;
            ctx.fill(lx - 2, ly - 2, lx + labelW + 2, ly + 10, 0xC0000000);
            ctx.text(mc.font, Component.literal(label), lx, ly, 0xFFFF6B6B, true);
        }

        tibiaChatTabs$drawUnreadPanel(ctx, mc, screenH);
    }

    private void tibiaChatTabs$drawUnreadPanel(GuiGraphicsExtractor ctx, Minecraft mc, int screenH) {
        int unread = TibiaChatTabsClient.CHAT.totalUnread();
        com.example.tibiachat.hud.UnreadNotifier.clearEntries();
        if (unread <= 0) {
            com.example.tibiachat.hud.UnreadNotifier.collapse();
            return;
        }

        float scale = HudLayout.notifIconScale();
        int baseX = HudLayout.notifIconX();
        int baseY = HudLayout.notifIconY(screenH);
        String badge = "\u2709 " + unread;
        int badgeW = mc.font.width(badge);
        int rowH = 12;

        com.example.tibiachat.hud.UnreadNotifier.addEntry(
                "__badge__",
                baseX,
                baseY,
                baseX + Math.round((badgeW + 6) * scale),
                baseY + Math.round(rowH * scale)
        );

        if (com.example.tibiachat.hud.UnreadNotifier.isExpanded()) {
            java.util.List<com.example.tibiachat.chat.Conversation> list =
                    com.example.tibiachat.hud.UnreadNotifier.unreadConversations();
            int panelW = 0;
            for (com.example.tibiachat.chat.Conversation c : list) {
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
            ctx.fill(-3, -3, panelW + 3, panelH + 3, 0xFF000000);
            ctx.fill(-2, -2, panelW + 2, panelH + 2, 0xFF1A1A1A);
            ctx.fill(-1, -1, panelW + 1, panelH + 1, 0xFF2A2A2A);

            int ly = 2;
            for (com.example.tibiachat.chat.Conversation c : list) {
                String line = c.playerName() + "  " + c.unread();
                ctx.fill(0, ly - 1, panelW, ly + rowH - 2, 0xFF3A6EA5);
                ctx.text(mc.font, Component.literal(line), 4, ly, 0xFFFFD24A, true);
                int absY0 = panelY + Math.round(ly * scale);
                int absY1 = panelY + Math.round((ly + rowH) * scale);
                com.example.tibiachat.hud.UnreadNotifier.addEntry(
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
        ctx.fill(-3, -2, badgeW + 3, 10, 0xFF000000);
        ctx.fill(-2, -1, badgeW + 2, 9, 0xFF303030);
        ctx.text(mc.font, Component.literal(badge), 0, 0, 0xFFFFD24A, true);
        ctx.pose().popMatrix();
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
            GuiGraphicsExtractor ctx,
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

        int tabAlpha = TibiaChatTabsClient.CONFIG.tabAlpha();
        int tabRgb = TibiaChatTabsClient.CONFIG.tabColor();
        int shadedRgb = selected
                ? tibiaChatTabs$lighten(tabRgb, 0.18f)
                : (hover ? tibiaChatTabs$lighten(tabRgb, 0.09f) : tabRgb);

        ctx.fill(
                x,
                top,
                x + w - 1,
                bottom,
                (tabAlpha << 24) | shadedRgb
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

        ctx.text(
                mc.font,
                Component.literal(shown),
                0,
                0,
                unread > 0 ? 0xFFFFD24A : 0xFFFFFFFF,
                true
        );

        ctx.pose().popMatrix();

        if (hover && !ConversationManager.MAIN.equals(key)) {
            int closeX = x + w - Math.round((CLOSE_SIZE + 2) * textScale);
            int closeY = top + 4;

            ctx.text(
                    mc.font,
                    Component.literal("×"),
                    closeX,
                    closeY - 1,
                    0xFFFFFFFF,
                    true
            );
        }

        return x + w;
    }

    private static int tibiaChatTabs$lighten(int rgb, float amount) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = Math.min(255, Math.round(r + (255 - r) * amount));
        g = Math.min(255, Math.round(g + (255 - g) * amount));
        b = Math.min(255, Math.round(b + (255 - b) * amount));
        return (r << 16) | (g << 8) | b;
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

        double amount = Math.abs(horizontalAmount) > Math.abs(verticalAmount) ? horizontalAmount : verticalAmount;

        if (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom) {
            int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
            int tabsRight = right - HudLayout.settingsBtnW() - 4;

            int maxScroll = tibiaChatTabs$maxTabScroll(mc, tabsLeft, tabsRight);
            if (maxScroll <= 0 || amount == 0) {
                return;
            }

            tibiaChatTabs$tabScroll -= (int) Math.round(amount * TAB_SCROLL_STEP);
            tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);

            cir.setReturnValue(true);
            return;
        }


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

        String unreadHit = com.example.tibiachat.hud.UnreadNotifier.handleClick(click.x(), click.y());
        if (unreadHit != null) {
            if (!unreadHit.isEmpty()) {
                tibiaChatTabs$select(unreadHit);
            }
            cir.setReturnValue(true);
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);
        int top = HudLayout.tabBarTop(screenH);
        int bottom = HudLayout.tabBarBottom(screenH);

        int sbX = HudLayout.settingsBtnX(screenW);
        int sbY = HudLayout.settingsBtnY(screenH);
        int sbW = HudLayout.settingsBtnW();
        int sbH = HudLayout.settingsBtnH();
        if (click.x() >= sbX && click.x() < sbX + sbW && click.y() >= sbY && click.y() < sbY + sbH) {
            mc.gui.setScreen(new TibiaChatConfigScreen((ChatScreen) (Object) this));
            cir.setReturnValue(true);
            return;
        }

        if (click.x() < left || click.x() >= right || click.y() < top || click.y() >= bottom) {
            return;
        }

        int mainW = tibiaChatTabs$tabMainWidth();
        int x = left + 2;

        if (click.x() >= x && click.x() < x + mainW) {
            tibiaChatTabs$dragKey = null;
            tibiaChatTabs$dragActive = false;
            tibiaChatTabs$select(ConversationManager.MAIN);
            cir.setReturnValue(true);
            return;
        }

        int tabsLeft = x + mainW;
        int tabsRight = right - HudLayout.settingsBtnW() - 4;

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
                    tibiaChatTabs$dragKey = c.key();
                    tibiaChatTabs$dragActive = false;
                    tibiaChatTabs$dragPressX = click.x();
                    tibiaChatTabs$dragPressY = click.y();
                    tibiaChatTabs$dragPointerOffsetX = (int) Math.round(click.x() - tabX);
                    tibiaChatTabs$select(c.key());
                }
                cir.setReturnValue(true);
                return;
            }

            tabX += w;
        }
    }

    @Inject(
            method = "mouseReleased",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void tibiaChatTabs$onMouseReleased(
            MouseButtonEvent click,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (tibiaChatTabs$dragKey == null) {
            return;
        }

        boolean wasDragging = tibiaChatTabs$dragActive;

        tibiaChatTabs$dragKey = null;
        tibiaChatTabs$dragActive = false;

        if (wasDragging) {
            cir.setReturnValue(true);
        }
    }

    private void tibiaChatTabs$tickDrag(
            Minecraft mc,
            int mouseX,
            int mouseY,
            int left,
            int right,
            int top,
            int bottom
    ) {
        if (tibiaChatTabs$dragKey == null) {
            return;
        }

        long window = mc.getWindow().handle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (!leftDown) {
            tibiaChatTabs$dragKey = null;
            tibiaChatTabs$dragActive = false;
            return;
        }

        if (!tibiaChatTabs$dragActive) {
            double dx = mouseX - tibiaChatTabs$dragPressX;
            double dy = mouseY - tibiaChatTabs$dragPressY;
            if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) {
                return;
            }
            tibiaChatTabs$dragActive = true;
        }

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - HudLayout.settingsBtnW() - 4;

        double pointerX = Math.max(tabsLeft, Math.min(tabsRight, (double) mouseX));

        tibiaChatTabs$updateDragOrder(mc, tabsLeft, tabsRight, pointerX);
    }

    private void tibiaChatTabs$updateDragOrder(Minecraft mc, int tabsLeft, int tabsRight, double pointerX) {
        ConversationManager conversations = TibiaChatTabsClient.CHAT.conversations();
        List<Conversation> order = conversations.asList();

        int currentIndex = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i).key().equals(tibiaChatTabs$dragKey)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            return;
        }

        double x = tabsLeft - tibiaChatTabs$tabScroll;
        int targetIndex = currentIndex;

        for (int i = 0; i < order.size(); i++) {
            int w = tibiaChatTabs$tabWidth(mc, order.get(i).playerName());
            double mid = x + w / 2.0;

            if (i == currentIndex) {
                x += w;
                continue;
            }

            if (i < currentIndex) {
                if (pointerX < mid) {
                    targetIndex = Math.min(targetIndex, i);
                }
            } else {
                if (pointerX > mid) {
                    targetIndex = Math.max(targetIndex, i);
                }
            }

            x += w;
        }

        if (targetIndex != currentIndex) {
            conversations.moveToIndex(tibiaChatTabs$dragKey, targetIndex);
        }
    }

    private void tibiaChatTabs$closeTab(String key) {
        if (key.equals(tibiaChatTabs$dragKey)) {
            tibiaChatTabs$dragKey = null;
            tibiaChatTabs$dragActive = false;
        }

        String selectedKey = TibiaChatTabsClient.CHAT.selectedKey();
        boolean selected = key.equals(selectedKey);

        TibiaChatTabsClient.CHAT.conversations().remove(key);
        com.example.tibiachat.chat.ChatPersistence.scheduleSave();

        if (!selected) {
            tibiaChatTabs$clampTabScrollAfterClose();
            return;
        }

        tibiaChatTabs$select(ConversationManager.MAIN);
    }

    private void tibiaChatTabs$clampTabScrollAfterClose() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - HudLayout.settingsBtnW() - 4;

        tibiaChatTabs$clampTabScroll(mc, tabsLeft, tabsRight);
    }



    private void tibiaChatTabs$select(String key) {
        if (TibiaChatTabsClient.CHAT.selectedKey().equals(key)) {
            return;
        }

        String previousKey = TibiaChatTabsClient.CHAT.selectedKey();
        TibiaChatTabsClient.CHAT.select(key);
        tibiaChatTabs$applyReplyPrefix(previousKey);
        tibiaChatTabs$scrollSelectedIntoView();
    }

    private void tibiaChatTabs$scrollSelectedIntoView() {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();

        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(screenW);

        int tabsLeft = left + 2 + tibiaChatTabs$tabMainWidth();
        int tabsRight = right - HudLayout.settingsBtnW() - 4;

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

    private String tibiaChatTabs$replyPrefix(String playerName) {
        String cmd = TibiaChatTabsClient.CONFIG.whisperCommand();
        if (cmd == null || cmd.isBlank()) {
            cmd = "/w";
        }
        if (!cmd.startsWith("/")) {
            cmd = "/" + cmd;
        }
        return cmd + " " + playerName + " ";
    }

    private void tibiaChatTabs$applyReplyPrefix(String previousKey) {
        if (this.input == null) {
            return;
        }

        String key = TibiaChatTabsClient.CHAT.selectedKey();
        if (ConversationManager.MAIN.equals(key)) {
            if (previousKey != null && !ConversationManager.MAIN.equals(previousKey)) {
                Conversation prev = TibiaChatTabsClient.CHAT.conversations().getByKey(previousKey);
                if (prev != null) {
                    String prefix = tibiaChatTabs$replyPrefix(prev.playerName());
                    String current = this.input.getValue();
                    if (current.startsWith(prefix) || current.equals(prefix.trim())) {
                        this.input.setValue("");
                    }
                } else {
                    String current = this.input.getValue();
                    String cmd = TibiaChatTabsClient.CONFIG.whisperCommand();
                    if (cmd == null || cmd.isBlank()) {
                        cmd = "/w";
                    }
                    if (!cmd.startsWith("/")) {
                        cmd = "/" + cmd;
                    }
                    if (current.startsWith(cmd + " ")) {
                        this.input.setValue("");
                    }
                }
            }
            return;
        }

        Conversation c = TibiaChatTabsClient.CHAT.conversations().getByKey(key);
        if (c == null) {
            return;
        }

        String prefix = tibiaChatTabs$replyPrefix(c.playerName());
        String current = this.input.getValue();
        if (!current.equals(prefix)) {
            this.input.setValue(prefix);
            this.input.setCursorPosition(prefix.length());
            this.input.setHighlightPos(prefix.length());
        }
    }


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private void tibiaChatTabs$keepOpenOnPrivateEnter(net.minecraft.client.input.KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (!TibiaChatTabsClient.CHAT.viewingPrivate()) {
            return;
        }
        int key = event.key();
        if (key != GLFW.GLFW_KEY_ENTER && key != GLFW.GLFW_KEY_KP_ENTER) {
            return;
        }
        if (this.input == null) {
            return;
        }
        Conversation c = TibiaChatTabsClient.CHAT.selectedConversation();
        String prefix = c != null ? tibiaChatTabs$replyPrefix(c.playerName()) : "";
        String value = this.input.getValue();
        String trimmed = value == null ? "" : value.trim();
        boolean empty = trimmed.isEmpty() || trimmed.equals(prefix.trim());
        if (!empty) {
            if (TibiaChatTabsClient.CHAT.isPrivateCooldownActive()) {
                cir.setReturnValue(true);
                return;
            }
            ChatScreen self = (ChatScreen) (Object) this;
            self.handleChatInput(value, true);
            TibiaChatTabsClient.CHAT.markPrivateSend();
        }
        if (c != null) {
            this.input.setValue(prefix);
            this.input.setCursorPosition(prefix.length());
            this.input.setHighlightPos(prefix.length());
        } else {
            this.input.setValue("");
        }
        cir.setReturnValue(true);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void tibiaChatTabs$onInit(CallbackInfo ci) {
        String pending = TibiaChatTabsClient.CHAT.consumePendingOpenTab();
        if (pending != null) {
            TibiaChatTabsClient.CHAT.select(pending);
        } else if (!ConversationManager.MAIN.equals(TibiaChatTabsClient.CHAT.selectedKey())) {
            TibiaChatTabsClient.CHAT.select(ConversationManager.MAIN);
        }
        tibiaChatTabs$applyReplyPrefix(null);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void tibiaChatTabs$onRemoved(CallbackInfo ci) {
        if (this.input != null) {
            String current = this.input.getValue();
            String cmd = TibiaChatTabsClient.CONFIG.whisperCommand();
            if (cmd == null || cmd.isBlank()) {
                cmd = "/w";
            }
            if (!cmd.startsWith("/")) {
                cmd = "/" + cmd;
            }
            if (current.startsWith(cmd + " ")) {
                this.input.setValue("");
            }
        }
        TibiaChatTabsClient.CHAT.resetToMain();
    }
}

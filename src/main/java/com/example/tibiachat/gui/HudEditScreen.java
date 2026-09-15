package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import com.example.tibiachat.hud.HudLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {

    private enum Target { NONE, TAB_BAR, TAB_BAR_RESIZE, ICON }

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;
    private Target dragging = Target.NONE;

    public HudEditScreen(Screen parent) {
        super(Component.literal("Reposition Chat Tabs HUD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 28, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x40000000);

        int chatBottom = this.height - 40;
        int chatLeft = 4;
        String[] samples = {
            "§7[System] Welcome to the server!",
            "§fPlayer123 §7» Hello everyone",
            "§dYou whisper to Friend: hey there",
            "§dFriend whispers: hi!",
            "§e[!] A new message arrived"
        };
        int lineH = 10;
        int startY = chatBottom - samples.length * lineH - 4;
        context.fill(chatLeft - 2, startY - 2, chatLeft + 220, chatBottom, 0x90000000);
        for (int i = 0; i < samples.length; i++) {
            context.drawString(this.font, Component.literal(samples[i]), chatLeft, startY + i * lineH, 0xFFFFFFFF, true);
        }
        context.drawString(this.font, Component.literal("§8(sample chat)"), chatLeft, chatBottom - 2, 0xFF888888, false);

        int barLeft = HudLayout.tabBarLeft();
        int barRight = HudLayout.tabBarRight(this.width);
        int barTop = HudLayout.tabBarTop(this.height);
        int barBottom = HudLayout.tabBarBottom(this.height);

        int barAlpha = config.tabBarAlpha();
        int barRgb = config.tabBarColor();
        context.fill(barLeft, barTop, barRight, barBottom, (barAlpha << 24) | barRgb);

        context.fill(barRight - 6, barTop, barRight, barBottom, 0xFF4A90E2);

        int tabAlpha = config.tabAlpha();
        int tabRgb = config.tabColor();
        int mainW = Math.round(50 * HudLayout.tabTextScale());
        context.fill(barLeft + 2, barTop + 2, barLeft + 2 + mainW, barBottom - 2, (tabAlpha << 24) | tabRgb);
        context.drawCenteredString(this.font, "Main", barLeft + 2 + mainW / 2, barTop + (barBottom - barTop) / 2 - 4, 0xFFFFFFFF);

        int tabW = Math.round(70 * HudLayout.tabTextScale());
        context.fill(barLeft + 4 + mainW, barTop + 2, barLeft + 4 + mainW + tabW, barBottom - 2, (tabAlpha << 24) | tabRgb);
        context.drawCenteredString(this.font, "Sample", barLeft + 4 + mainW + tabW / 2, barTop + (barBottom - barTop) / 2 - 4, 0xFFFFFFFF);

        String sample = "\u2709 3";
        int textW = this.font.width(sample);
        int iconX = HudLayout.notifIconX();
        int iconY = HudLayout.notifIconY(this.height);
        int iconW = HudLayout.notifIconWidth(textW);
        int iconH = HudLayout.notifIconHeight();

        context.fill(iconX - 3, iconY - 2, iconX + iconW, iconY + iconH, 0xB0C77A2A);
        context.drawString(this.font, Component.literal(sample), iconX, iconY, 0xFFFFD24A, true);

        context.drawCenteredString(this.font,
                Component.literal("Drag elements to move. Drag blue edge to resize. Sample chat shown for reference."),
                this.width / 2, 8, 0xFFFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    private boolean withinResizeHandle(double x, double y) {
        int top = HudLayout.tabBarTop(this.height);
        int bottom = HudLayout.tabBarBottom(this.height);
        int right = HudLayout.tabBarRight(this.width);
        return x >= right - 8 && x <= right + 8 && y >= top && y < bottom;
    }

    private boolean withinTabBar(double x, double y) {
        int top = HudLayout.tabBarTop(this.height);
        int bottom = HudLayout.tabBarBottom(this.height);
        int left = HudLayout.tabBarLeft();
        int right = HudLayout.tabBarRight(this.width);
        return x >= left && x < right && y >= top && y < bottom;
    }

    private boolean withinIcon(double x, double y) {
        String sample = "\u2709 3";
        int textW = this.font.width(sample);
        int iconX = HudLayout.notifIconX();
        int iconY = HudLayout.notifIconY(this.height);
        int iconW = HudLayout.notifIconWidth(textW);
        int iconH = HudLayout.notifIconHeight();
        return x >= iconX - 3 && x < iconX + iconW && y >= iconY - 2 && y < iconY + iconH;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (withinIcon(click.x(), click.y())) {
            dragging = Target.ICON;
            return true;
        }
        if (withinResizeHandle(click.x(), click.y())) {
            dragging = Target.TAB_BAR_RESIZE;
            return true;
        }
        if (withinTabBar(click.x(), click.y())) {
            dragging = Target.TAB_BAR;
            return true;
        }
        dragging = Target.NONE;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        switch (dragging) {
            case TAB_BAR -> {
                config.setTabBarOffsetX(Math.round(config.tabBarOffsetX() + (float) offsetX));
                config.setTabBarOffsetY(Math.round(config.tabBarOffsetY() + (float) offsetY));
                return true;
            }
            case TAB_BAR_RESIZE -> {
                config.setTabBarWidth(Math.max(50, Math.round(config.tabBarWidth() + (float) offsetX)));
                return true;
            }
            case ICON -> {
                config.setNotifIconOffsetX(Math.round(config.notifIconOffsetX() + (float) offsetX));
                config.setNotifIconOffsetY(Math.round(config.notifIconOffsetY() + (float) offsetY));
                return true;
            }
            default -> {
                return super.mouseDragged(click, offsetX, offsetY);
            }
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        boolean wasDragging = dragging != Target.NONE;
        dragging = Target.NONE;
        if (wasDragging) {
            config.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}

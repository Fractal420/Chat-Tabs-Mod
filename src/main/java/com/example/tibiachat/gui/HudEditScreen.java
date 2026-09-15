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

    private enum Target { NONE, TAB_BAR, TAB_BAR_RESIZE, ICON, SETTINGS_BTN }

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;
    private Target dragging = Target.NONE;
    private double dragStartMouseX;
    private double dragStartMouseY;
    private int dragStartOffsetX;
    private int dragStartOffsetY;
    private int dragStartWidth;

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
        context.fill(0, 0, this.width, this.height, 0x60000000);

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
        float iconScale = HudLayout.notifIconScale();
        int iconW = Math.max(Math.round((textW + 6) * iconScale), 20);
        int iconH = Math.max(Math.round(12 * iconScale), 14);
        context.fill(iconX - 3, iconY - 2, iconX + iconW, iconY + iconH, 0xB0C77A2A);
        context.pose().pushMatrix();
        context.pose().translate(iconX, iconY);
        context.pose().scale(iconScale, iconScale);
        context.drawString(this.font, Component.literal(sample), 0, 0, 0xFFFFD24A, true);
        context.pose().popMatrix();

        int sbX = HudLayout.settingsBtnX(this.width);
        int sbY = HudLayout.settingsBtnY(this.height);
        int sbW = HudLayout.settingsBtnW();
        int sbH = HudLayout.settingsBtnH();
        context.fill(sbX, sbY, sbX + sbW, sbY + sbH, 0xFF3A6EA5);
        context.drawString(this.font, Component.literal("\u2699"), sbX + 3, sbY + 3, 0xFFFFFFFF, true);

        context.drawCenteredString(this.font,
                Component.literal("Drag tab bar / notif icon / settings button. Blue edge resizes tab bar."),
                this.width / 2, 12, 0xFFFFFFFF);

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
        float iconScale = HudLayout.notifIconScale();
        int iconW = Math.max(Math.round((textW + 6) * iconScale), 20);
        int iconH = Math.max(Math.round(12 * iconScale), 14);
        return x >= iconX - 6 && x < iconX + iconW + 6 && y >= iconY - 6 && y < iconY + iconH + 6;
    }

    private boolean withinSettingsBtn(double x, double y) {
        int sbX = HudLayout.settingsBtnX(this.width);
        int sbY = HudLayout.settingsBtnY(this.height);
        int sbW = HudLayout.settingsBtnW();
        int sbH = HudLayout.settingsBtnH();
        return x >= sbX && x < sbX + sbW && y >= sbY && y < sbY + sbH;
    }

    private void beginDrag(Target target, double mouseX, double mouseY) {
        dragging = target;
        dragStartMouseX = mouseX;
        dragStartMouseY = mouseY;
        switch (target) {
            case TAB_BAR -> {
                dragStartOffsetX = config.tabBarOffsetX();
                dragStartOffsetY = config.tabBarOffsetY();
            }
            case TAB_BAR_RESIZE -> dragStartWidth = config.tabBarWidth();
            case ICON -> {
                dragStartOffsetX = config.notifIconOffsetX();
                dragStartOffsetY = config.notifIconOffsetY();
            }
            case SETTINGS_BTN -> {
                dragStartOffsetX = config.settingsBtnOffsetX();
                dragStartOffsetY = config.settingsBtnOffsetY();
            }
            default -> {}
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double x = click.x();
        double y = click.y();
        if (withinIcon(x, y)) {
            beginDrag(Target.ICON, x, y);
            return true;
        }
        if (withinSettingsBtn(x, y)) {
            beginDrag(Target.SETTINGS_BTN, x, y);
            return true;
        }
        if (withinResizeHandle(x, y)) {
            beginDrag(Target.TAB_BAR_RESIZE, x, y);
            return true;
        }
        if (withinTabBar(x, y)) {
            beginDrag(Target.TAB_BAR, x, y);
            return true;
        }
        dragging = Target.NONE;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (dragging == Target.NONE) {
            return super.mouseDragged(click, offsetX, offsetY);
        }
        double dx = click.x() - dragStartMouseX;
        double dy = click.y() - dragStartMouseY;
        switch (dragging) {
            case TAB_BAR -> {
                config.setTabBarOffsetX((int) Math.round(dragStartOffsetX + dx));
                config.setTabBarOffsetY((int) Math.round(dragStartOffsetY + dy));
            }
            case TAB_BAR_RESIZE -> config.setTabBarWidth(Math.max(50, (int) Math.round(dragStartWidth + dx)));
            case ICON -> {
                config.setNotifIconOffsetX((int) Math.round(dragStartOffsetX + dx));
                config.setNotifIconOffsetY((int) Math.round(dragStartOffsetY + dy));
            }
            case SETTINGS_BTN -> {
                config.setSettingsBtnOffsetX((int) Math.round(dragStartOffsetX + dx));
                config.setSettingsBtnOffsetY((int) Math.round(dragStartOffsetY + dy));
            }
            default -> {}
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        boolean was = dragging != Target.NONE;
        dragging = Target.NONE;
        if (was) {
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

package com.example.tibiachat.hud;

import com.example.tibiachat.TibiaChatTabsClient;

public final class HudLayout {

    public static final int BASE_TAB_HEIGHT = 16;
    public static final int BASE_TAB_BOTTOM_OFFSET = 30;
    public static final int BASE_ICON_X = 6;
    public static final int BASE_ICON_Y_FROM_BOTTOM = 66;
    public static final int SETTINGS_BTN_W = 16;

    private HudLayout() {}

    public static int tabBarHeight() {
        return Math.max(8, Math.round(BASE_TAB_HEIGHT * TibiaChatTabsClient.CONFIG.tabBarScale()));
    }

    public static int tabBarWidth(int screenWidth) {
        return Math.min(screenWidth, TibiaChatTabsClient.CONFIG.tabBarWidth());
    }

    public static int tabBarLeft() {
        return TibiaChatTabsClient.CONFIG.tabBarOffsetX();
    }

    public static int tabBarRight(int screenWidth) {
        return tabBarLeft() + tabBarWidth(screenWidth);
    }

    public static int tabBarTop(int screenHeight) {
        return screenHeight - BASE_TAB_BOTTOM_OFFSET - tabBarHeight() + TibiaChatTabsClient.CONFIG.tabBarOffsetY();
    }

    public static int tabBarBottom(int screenHeight) {
        return tabBarTop(screenHeight) + tabBarHeight();
    }

    public static float tabTextScale() {
        return TibiaChatTabsClient.CONFIG.tabBarScale();
    }

    public static float notifIconScale() {
        return TibiaChatTabsClient.CONFIG.notifIconScale();
    }

    public static int notifIconX() {
        return BASE_ICON_X + TibiaChatTabsClient.CONFIG.notifIconOffsetX();
    }

    public static int notifIconY(int screenHeight) {
        return screenHeight - BASE_ICON_Y_FROM_BOTTOM + TibiaChatTabsClient.CONFIG.notifIconOffsetY();
    }

    public static int notifIconWidth(int textWidth) {
        return Math.round((textWidth + 6) * notifIconScale());
    }

    public static int notifIconHeight() {
        return Math.round(12 * notifIconScale());
    }

    public static int settingsBtnX(int screenWidth) {
        return tabBarRight(screenWidth) - SETTINGS_BTN_W + TibiaChatTabsClient.CONFIG.settingsBtnOffsetX();
    }

    public static int settingsBtnY(int screenHeight) {
        return tabBarTop(screenHeight) + TibiaChatTabsClient.CONFIG.settingsBtnOffsetY();
    }

    public static int settingsBtnW() {
        return SETTINGS_BTN_W;
    }

    public static int settingsBtnH() {
        return tabBarHeight();
    }
}

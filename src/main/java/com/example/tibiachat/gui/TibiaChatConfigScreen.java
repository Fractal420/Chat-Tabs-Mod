package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.ChatPersistence;
import com.example.tibiachat.chat.NotificationSounds;
import com.example.tibiachat.chat.SentMessageHistory;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TibiaChatConfigScreen extends Screen {

    private static final int ROW = 26;
    private static final int SLIDER_H = 18;
    private static final int SLIDER_W = 120;
    private static final int FIELD_W = 40;
    private static final int FIELD_GAP = 6;
    private static final int HALF_W = 170;
    private static final int COL_GAP = 12;
    private static final int FULL_W = HALF_W * 2 + COL_GAP;
    private static final String[] TABS = {"Appearance", "Persistence", "Private"};

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;
    private int activeTab;
    private Button themeButton;


    public TibiaChatConfigScreen(Screen parent) {
        super(Component.literal("Chat Tabs Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int left = cx - FULL_W / 2;
        int right = left + HALF_W + COL_GAP;
        int tabY = 24;
        int tabW = FULL_W / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            final int tabIndex = i;
            boolean selected = i == activeTab;
            String label = (selected ? "> " : "") + TABS[i] + (selected ? " <" : "");
            this.addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                activeTab = tabIndex;
                this.rebuildWidgets();
            }).bounds(left + i * tabW, tabY, tabW - 2, 18).build());
        }

        int y = tabY + 28;

        switch (activeTab) {
            case 0 -> buildAppearance(left, right, y);
            case 1 -> buildPersistence(left, right, y);
            case 2 -> buildPrivate(left, right, y);
            default -> {}
        }

        int footerY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.resetAllDefaults();
            config.save();
            SentMessageHistory.onLimitChanged();
            TibiaChatTabsClient.CHAT.trimMain();
            ChatPersistence.scheduleSave();
            this.rebuildWidgets();
        }).bounds(left, footerY, HALF_W, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(right, footerY, HALF_W, 18).build());
    }

    private void buildAppearance(int left, int right, int y) {
        int yL = y;
        int yR = y;
        yL = addSlider(left, yL, "Tab bar size", TibiaChatConfig.MIN_BAR_SCALE, TibiaChatConfig.MAX_BAR_SCALE,
                config.tabBarScale(), false, "%.2fx", v -> config.setTabBarScale((float) v));
        yL = addSlider(left, yL, "Icon size", TibiaChatConfig.MIN_ICON_SCALE, TibiaChatConfig.MAX_ICON_SCALE,
                config.notifIconScale(), false, "%.2fx", v -> config.setNotifIconScale((float) v));
        yR = addSlider(right, yR, "Bar opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabBarAlpha(), true, "%.0f", v -> config.setTabBarAlpha((int) v));
        yR = addSlider(right, yR, "Tab opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabAlpha(), true, "%.0f", v -> config.setTabAlpha((int) v));
        y = Math.max(yL, yR) + 6;

        themeButton = Button.builder(themeLabel(), btn -> {
            config.cycleColorTheme();
            config.save();
            btn.setMessage(themeLabel());
        }).bounds(left, y, FULL_W, 18).build();
        this.addRenderableWidget(themeButton);
        y += ROW + 4;

        this.addRenderableWidget(Button.builder(Component.literal("Reposition / Resize HUD"),
                btn -> openHudEdit())
                .bounds(left, y, FULL_W, 18).build());
        y += ROW + 4;

        this.addRenderableWidget(Button.builder(Component.literal("Message Detection"),
                btn -> openDetection())
                .bounds(left, y, FULL_W, 18).build());
    }

    private void buildPersistence(int left, int right, int y) {
        this.addRenderableWidget(Button.builder(
                Component.literal("Sent history: " + onOff(config.persistSentMessages())),
                btn -> {
                    config.setPersistSentMessages(!config.persistSentMessages());
                    config.save();
                    if (config.persistSentMessages()) {
                        SentMessageHistory.applyToChat();
                    }
                    this.rebuildWidgets();
                }).bounds(left, y, HALF_W, 18).build());
        this.addRenderableWidget(Button.builder(
                Component.literal("Chat log: " + onOff(config.persistChat())),
                btn -> {
                    config.setPersistChat(!config.persistChat());
                    config.save();
                    if (config.persistChat()) {
                        ChatPersistence.applyToHud();
                    }
                    this.rebuildWidgets();
                }).bounds(right, y, HALF_W, 18).build());
        y += ROW + 4;

        this.addRenderableWidget(Button.builder(
                Component.literal("Private tabs: " + onOff(config.persistTabs())),
                btn -> {
                    config.setPersistTabs(!config.persistTabs());
                    config.save();
                    this.rebuildWidgets();
                }).bounds(left, y, FULL_W, 18).build());
        y += ROW + 6;

        int yL = y;
        int yR = y;
        yL = addSlider(left, yL, "Sent limit",
                TibiaChatConfig.MIN_SENT_HISTORY, TibiaChatConfig.MAX_SENT_HISTORY,
                config.sentMessageHistoryLimit(), true, "%.0f",
                v -> {
                    config.setSentMessageHistoryLimit((int) v);
                    SentMessageHistory.onLimitChanged();
                });
        yR = addSlider(right, yR, "Chat limit",
                TibiaChatConfig.MIN_CHAT_HISTORY, TibiaChatConfig.MAX_CHAT_HISTORY,
                config.chatHistoryLimit(), true, "%.0f",
                v -> {
                    config.setChatHistoryLimit((int) v);
                    TibiaChatTabsClient.CHAT.trimMain();
                    ChatPersistence.scheduleSave();
                });
    }

    private void buildPrivate(int left, int right, int y) {
        y = addSlider(left, y, "Send cooldown",
                TibiaChatConfig.MIN_WHISPER_COOLDOWN, TibiaChatConfig.MAX_WHISPER_COOLDOWN,
                config.whisperCooldownSeconds(), true, "%.0fs",
                v -> config.setWhisperCooldownSeconds((int) v));
        y += 6;

        this.addRenderableWidget(Button.builder(
                Component.literal("Sound: " + onOff(config.notifSoundEnabled())),
                btn -> {
                    config.setNotifSoundEnabled(!config.notifSoundEnabled());
                    config.save();
                    this.rebuildWidgets();
                }).bounds(left, y, HALF_W, 18).build());
        this.addRenderableWidget(Button.builder(
                Component.literal("Tone: " + NotificationSounds.label(config.notifSoundIndex())),
                btn -> {
                    int next = NotificationSounds.nextIndex(config.notifSoundIndex());
                    config.setNotifSoundIndex(next);
                    config.save();
                    NotificationSounds.play(next, config.notifSoundVolume() / 100.0f);
                    this.rebuildWidgets();
                }).bounds(right, y, HALF_W, 18).build());
        y += ROW + 4;

        addSlider(left, y, "Sound volume",
                TibiaChatConfig.MIN_NOTIF_VOLUME, TibiaChatConfig.MAX_NOTIF_VOLUME,
                config.notifSoundVolume(), true, "%.0f%%",
                v -> config.setNotifSoundVolume((int) v));
    }





    private void openHudEdit() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new HudEditScreen(this));
        }
    }

    private void openDetection() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new ChatRegexConfigScreen(this));
        }
    }

    private static String onOff(boolean value) {
        return value ? "ON" : "OFF";
    }

    private Component themeLabel() {
        return Component.literal("Theme: " + config.colorTheme() + "  (click to cycle)");
    }

    private int addSlider(int x, int y, String label, double min, double max, double initial,
                          boolean whole, String fmt, DoubleConsumer apply) {
        ValueSlider slider = new ValueSlider(x, y, SLIDER_W, SLIDER_H, min, max, initial, whole,
                v -> Component.literal(label + ": " + String.format(Locale.ROOT, fmt, v)),
                v -> { apply.accept(v); config.save(); });
        EditBox field = new EditBox(this.font, x + SLIDER_W + FIELD_GAP, y, FIELD_W, SLIDER_H, Component.literal(label));
        field.setValue(String.format(Locale.ROOT, whole ? "%.0f" : "%.2f", initial));
        field.setResponder(text -> {
            try {
                double parsed = Double.parseDouble(text.trim());
                double clamped = Math.max(min, Math.min(max, parsed));
                slider.setRealValueSilently(clamped);
                apply.accept(clamped);
                config.save();
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(slider);
        this.addRenderableWidget(field);
        return y + ROW;
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

}

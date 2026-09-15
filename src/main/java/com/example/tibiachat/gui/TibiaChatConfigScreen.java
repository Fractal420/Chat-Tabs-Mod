package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.chat.ChatPersistence;
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

    private static final int ROW = 18;
    private static final int SLIDER_W = 130;
    private static final int FIELD_W = 36;
    private static final int GAP = 4;
    private static final int BTN_W = 210;
    private static final int HALF_BTN = 102;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;
    private Button themeButton;

    public TibiaChatConfigScreen(Screen parent) {
        super(Component.literal("Chat Tabs Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int left = cx - 168;
        int right = cx + 8;
        int y = 26;

        y = addSlider(left, y, "Tab bar size", TibiaChatConfig.MIN_BAR_SCALE, TibiaChatConfig.MAX_BAR_SCALE,
                config.tabBarScale(), false, "%.2fx", v -> config.setTabBarScale((float) v));
        y = addSlider(left, y, "Icon size", TibiaChatConfig.MIN_ICON_SCALE, TibiaChatConfig.MAX_ICON_SCALE,
                config.notifIconScale(), false, "%.2fx", v -> config.setNotifIconScale((float) v));

        int yRight = 26;
        yRight = addSlider(right, yRight, "Bar opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabBarAlpha(), true, "%.0f", v -> config.setTabBarAlpha((int) v));
        yRight = addSlider(right, yRight, "Tab opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabAlpha(), true, "%.0f", v -> config.setTabAlpha((int) v));

        y = Math.max(y, yRight) + 4;

        themeButton = Button.builder(themeLabel(), btn -> {
            config.cycleColorTheme();
            config.save();
            btn.setMessage(themeLabel());
        }).bounds(cx - BTN_W / 2, y, BTN_W, 16).build();
        this.addRenderableWidget(themeButton);
        y += ROW;

        this.addRenderableWidget(Button.builder(Component.literal("Reposition / Resize HUD"),
                btn -> { if (this.minecraft != null) this.minecraft.setScreen(new HudEditScreen(this)); })
                .bounds(cx - BTN_W / 2, y, BTN_W, 16).build());
        y += ROW;

        this.addRenderableWidget(Button.builder(Component.literal("Message Detection"),
                btn -> { if (this.minecraft != null) this.minecraft.setScreen(new ChatRegexConfigScreen(this)); })
                .bounds(cx - BTN_W / 2, y, BTN_W, 16).build());
        y += ROW + 4;

        this.addRenderableWidget(Button.builder(
                Component.literal("Persist sent: " + (config.persistSentMessages() ? "ON" : "OFF")),
                btn -> {
                    config.setPersistSentMessages(!config.persistSentMessages());
                    config.save();
                    if (config.persistSentMessages()) {
                        SentMessageHistory.applyToChat();
                    }
                    this.rebuildWidgets();
                }).bounds(left, y, HALF_BTN + 40, 16).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("Persist chat: " + (config.persistChat() ? "ON" : "OFF")),
                btn -> {
                    config.setPersistChat(!config.persistChat());
                    config.save();
                    if (config.persistChat()) {
                        ChatPersistence.applyToHud();
                    }
                    this.rebuildWidgets();
                }).bounds(right, y, HALF_BTN + 40, 16).build());
        y += ROW;

        this.addRenderableWidget(Button.builder(
                Component.literal("Persist tabs: " + (config.persistTabs() ? "ON" : "OFF")),
                btn -> {
                    config.setPersistTabs(!config.persistTabs());
                    config.save();
                    this.rebuildWidgets();
                }).bounds(cx - BTN_W / 2, y, BTN_W, 16).build());
        y += ROW + 2;

        y = addSlider(left, y, "Sent history",
                TibiaChatConfig.MIN_SENT_HISTORY, TibiaChatConfig.MAX_SENT_HISTORY,
                config.sentMessageHistoryLimit(), true, "%.0f",
                v -> {
                    config.setSentMessageHistoryLimit((int) v);
                    SentMessageHistory.onLimitChanged();
                });

        addSlider(right, y - ROW, "Chat history",
                TibiaChatConfig.MIN_CHAT_HISTORY, TibiaChatConfig.MAX_CHAT_HISTORY,
                config.chatHistoryLimit(), true, "%.0f",
                v -> {
                    config.setChatHistoryLimit((int) v);
                    TibiaChatTabsClient.CHAT.trimMain();
                    ChatPersistence.scheduleSave();
                });
        y += 6;

        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.resetAllDefaults();
            config.save();
            SentMessageHistory.onLimitChanged();
            TibiaChatTabsClient.CHAT.trimMain();
            ChatPersistence.scheduleSave();
            this.rebuildWidgets();
        }).bounds(cx - BTN_W / 2, y, BTN_W, 16).build());
        y += ROW + 2;

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(cx - BTN_W / 2, y, BTN_W, 16).build());
    }

    private Component themeLabel() {
        return Component.literal("Theme: " + config.colorTheme() + "  (click to cycle)");
    }

    private int addSlider(int x, int y, String label, double min, double max, double initial,
                          boolean whole, String fmt, DoubleConsumer apply) {
        ValueSlider slider = new ValueSlider(x, y, SLIDER_W, 14, min, max, initial, whole,
                v -> Component.literal(label + ": " + String.format(Locale.ROOT, fmt, v)),
                v -> { apply.accept(v); config.save(); });
        EditBox field = new EditBox(this.font, x + SLIDER_W + GAP, y, FIELD_W, 14, Component.literal(label));
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
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}

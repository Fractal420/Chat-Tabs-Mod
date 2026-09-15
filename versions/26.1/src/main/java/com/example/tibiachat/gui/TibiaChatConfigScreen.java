package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TibiaChatConfigScreen extends Screen {

    private static final int ROW = 20;
    private static final int SLIDER_W = 140;
    private static final int FIELD_W = 40;
    private static final int GAP = 4;

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
        int left = cx - 160;
        int right = cx + 20;
        int y = 28;

        y = addSlider(left, y, "Tab bar size", TibiaChatConfig.MIN_BAR_SCALE, TibiaChatConfig.MAX_BAR_SCALE,
                config.tabBarScale(), false, "%.2fx", v -> config.setTabBarScale((float) v));
        y = addSlider(left, y, "Icon size", TibiaChatConfig.MIN_ICON_SCALE, TibiaChatConfig.MAX_ICON_SCALE,
                config.notifIconScale(), false, "%.2fx", v -> config.setNotifIconScale((float) v));

        int y2 = 28;
        y2 = addSlider(right, y2, "Bar opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabBarAlpha(), true, "%.0f", v -> config.setTabBarAlpha((int) v));
        y2 = addSlider(right, y2, "Tab opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabAlpha(), true, "%.0f", v -> config.setTabAlpha((int) v));

        y = Math.max(y, y2) + 6;

        themeButton = Button.builder(themeLabel(), btn -> {
            config.cycleColorTheme();
            config.save();
            btn.setMessage(themeLabel());
        }).bounds(cx - 110, y, 220, 18).build();
        this.addRenderableWidget(themeButton);
        y += ROW + 4;

        this.addRenderableWidget(Button.builder(Component.literal("Reposition / Resize HUD"),
                btn -> { if (this.minecraft != null) this.minecraft.setScreen(new HudEditScreen(this)); })
                .bounds(cx - 110, y, 220, 18).build());
        y += ROW + 2;

        this.addRenderableWidget(Button.builder(Component.literal("PM Detection Rules"),
                btn -> { if (this.minecraft != null) this.minecraft.setScreen(new ChatRegexConfigScreen(this)); })
                .bounds(cx - 110, y, 220, 18).build());
        y += ROW + 8;

        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.resetAllDefaults();
            config.save();
            this.rebuildWidgets();
        }).bounds(cx - 100, y, 200, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(cx - 100, this.height - 26, 200, 18).build());
    }

    private Component themeLabel() {
        return Component.literal("Theme: " + config.colorTheme() + "  (click to cycle)");
    }

    private int addSlider(int x, int y, String label, double min, double max, double initial,
                          boolean whole, String fmt, DoubleConsumer apply) {
        ValueSlider slider = new ValueSlider(x, y, SLIDER_W, 16, min, max, initial, whole,
                v -> Component.literal(label + ": " + String.format(Locale.ROOT, fmt, v)),
                v -> { apply.accept(v); config.save(); });
        EditBox field = new EditBox(this.font, x + SLIDER_W + GAP, y, FIELD_W, 16, Component.literal(label));
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
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }
}

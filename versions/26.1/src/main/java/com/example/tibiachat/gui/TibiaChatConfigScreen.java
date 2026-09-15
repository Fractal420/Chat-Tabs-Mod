package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TibiaChatConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 22;
    private static final int SLIDER_WIDTH = 160;
    private static final int FIELD_WIDTH = 42;
    private static final int FIELD_GAP = 4;
    private static final int PREVIEW_HEIGHT = 28;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;

    public TibiaChatConfigScreen(Screen parent) {
        super(Component.literal("Chat Tabs Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 36 + PREVIEW_HEIGHT + 8;

        y = addSliderRow(centerX, y, "Tab bar size", TibiaChatConfig.MIN_BAR_SCALE, TibiaChatConfig.MAX_BAR_SCALE,
                config.tabBarScale(), false, "%.2fx",
                v -> config.setTabBarScale((float) v));
        y = addSliderRow(centerX, y, "Notif icon size", TibiaChatConfig.MIN_ICON_SCALE, TibiaChatConfig.MAX_ICON_SCALE,
                config.notifIconScale(), false, "%.2fx",
                v -> config.setNotifIconScale((float) v));
        y += 4;

        this.addRenderableWidget(Button.builder(Component.literal("Reposition / Resize HUD"),
                        btn -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new HudEditScreen(this));
                            }
                        })
                .bounds(centerX - 110, y, 220, 20)
                .build());
        y += ROW_HEIGHT + 6;

        y = addSliderRow(centerX, y, "Tab bar opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabBarAlpha(), true, "%.0f",
                v -> config.setTabBarAlpha((int) v));
        y = addSliderRow(centerX, y, "Tab opacity", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabAlpha(), true, "%.0f",
                v -> config.setTabAlpha((int) v));
        y += 4;

        y = addColorPickerRow(centerX, y, "Tab bar color", config.tabBarColor(),
                v -> config.setTabBarColor(v));
        y = addColorPickerRow(centerX, y, "Tab color", config.tabColor(),
                v -> config.setTabColor(v));
        y += 6;

        this.addRenderableWidget(Button.builder(Component.literal("Edit Detection Rules"),
                        btn -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new ChatRegexConfigScreen(this));
                            }
                        })
                .bounds(centerX - 110, y, 220, 20)
                .build());
        y += ROW_HEIGHT + 10;

        this.addRenderableWidget(Button.builder(Component.literal("Reset All Defaults"), btn -> {
                    config.resetHudDefaults();
                    config.resetAppearanceDefaults();
                    config.save();
                    this.rebuildWidgets();
                })
                .bounds(centerX - 100, y, 200, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(centerX - 100, this.height - 28, 200, 20)
                .build());
    }

    private int addSliderRow(int centerX, int y, String label, double min, double max, double initial,
                              boolean wholeNumber, String format, DoubleConsumer apply) {
        int totalW = SLIDER_WIDTH + FIELD_GAP + FIELD_WIDTH;
        int sliderX = centerX - totalW / 2;
        int fieldX = sliderX + SLIDER_WIDTH + FIELD_GAP;

        ValueSlider slider = new ValueSlider(sliderX, y, SLIDER_WIDTH, 18, min, max, initial, wholeNumber,
                v -> Component.literal(label + ": " + String.format(Locale.ROOT, format, v)),
                v -> {
                    apply.accept(v);
                    config.save();
                });

        EditBox field = new EditBox(this.font, fieldX, y, FIELD_WIDTH, 18, Component.literal(label));
        field.setValue(String.format(Locale.ROOT, wholeNumber ? "%.0f" : "%.2f", initial));
        field.setResponder(text -> {
            try {
                double parsed = Double.parseDouble(text.trim());
                double clamped = Math.max(min, Math.min(max, parsed));
                slider.setRealValueSilently(clamped);
                apply.accept(clamped);
                config.save();
            } catch (NumberFormatException ignored) {
            }
        });

        this.addRenderableWidget(slider);
        this.addRenderableWidget(field);
        return y + ROW_HEIGHT;
    }

    private int addColorPickerRow(int centerX, int y, String label, int initialRgb, IntConsumer apply) {
        int r = (initialRgb >> 16) & 0xFF;
        int g = (initialRgb >> 8) & 0xFF;
        int b = initialRgb & 0xFF;

        int totalW = 280;
        int startX = centerX - totalW / 2;
        int sliderW = 70;
        int gap = 4;

        ValueSlider rSlider = new ValueSlider(startX, y, sliderW, 16, 0, 255, r, true,
                v -> Component.literal("R:" + (int) v),
                v -> {
                    int cur = label.contains("bar") ? config.tabBarColor() : config.tabColor();
                    int nr = (int) v;
                    int ng = (cur >> 8) & 0xFF;
                    int nb = cur & 0xFF;
                    apply.accept((nr << 16) | (ng << 8) | nb);
                    config.save();
                });
        this.addRenderableWidget(rSlider);

        ValueSlider gSlider = new ValueSlider(startX + sliderW + gap, y, sliderW, 16, 0, 255, g, true,
                v -> Component.literal("G:" + (int) v),
                v -> {
                    int cur = label.contains("bar") ? config.tabBarColor() : config.tabColor();
                    int nr = (cur >> 16) & 0xFF;
                    int ng = (int) v;
                    int nb = cur & 0xFF;
                    apply.accept((nr << 16) | (ng << 8) | nb);
                    config.save();
                });
        this.addRenderableWidget(gSlider);

        ValueSlider bSlider = new ValueSlider(startX + 2 * (sliderW + gap), y, sliderW, 16, 0, 255, b, true,
                v -> Component.literal("B:" + (int) v),
                v -> {
                    int cur = label.contains("bar") ? config.tabBarColor() : config.tabColor();
                    int nr = (cur >> 16) & 0xFF;
                    int ng = (cur >> 8) & 0xFF;
                    int nb = (int) v;
                    apply.accept((nr << 16) | (ng << 8) | nb);
                    config.save();
                });
        this.addRenderableWidget(bSlider);

        EditBox hex = new EditBox(this.font, startX + 3 * (sliderW + gap), y, 52, 16, Component.literal("hex"));
        hex.setMaxLength(7);
        hex.setValue(String.format(Locale.ROOT, "%06X", initialRgb));
        hex.setResponder(text -> {
            String h = text.trim();
            if (h.startsWith("#")) h = h.substring(1);
            if (!h.matches("[0-9a-fA-F]{6}")) return;
            try {
                int parsed = Integer.parseInt(h, 16);
                apply.accept(parsed);
                config.save();
            } catch (NumberFormatException ignored) {
            }
        });
        this.addRenderableWidget(hex);

        return y + ROW_HEIGHT;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.text(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);

        int previewTop = 24;
        int previewLeft = this.width / 2 - 160;
        int previewRight = this.width / 2 + 160;
        int barH = PREVIEW_HEIGHT - 4;

        int barAlpha = config.tabBarAlpha();
        int barRgb = config.tabBarColor();
        context.fill(previewLeft, previewTop, previewRight, previewTop + barH, (barAlpha << 24) | barRgb);

        int tabAlpha = config.tabAlpha();
        int tabRgb = config.tabColor();
        int tabW = 70;
        context.fill(previewLeft + 4, previewTop + 3, previewLeft + 4 + tabW, previewTop + barH - 3, (tabAlpha << 24) | tabRgb);
        context.text(this.font, "Main", previewLeft + 4 + tabW / 2 - this.font.width("Main") / 2, previewTop + 8, 0xFFFFFFFF);

        context.fill(previewLeft + 8 + tabW, previewTop + 3, previewLeft + 8 + 2 * tabW, previewTop + barH - 3, (tabAlpha << 24) | tabRgb);
        context.text(this.font, "Player", previewLeft + 8 + tabW + tabW / 2 - this.font.width("Player") / 2, previewTop + 8, 0xFFFFFFFF);

        context.text(this.font, "Preview", previewRight - 40, previewTop + 8, 0xFFAAAAAA);

        int centerX = this.width / 2;
        int y = 36 + PREVIEW_HEIGHT + 8;
        context.text(this.font, "— HUD Size —", centerX - this.font.width("— HUD Size —") / 2, y - 12, 0xFF88CCFF);
        y += 2 * ROW_HEIGHT + 4 + ROW_HEIGHT + 6;
        context.text(this.font, "— Appearance —", centerX - this.font.width("— Appearance —") / 2, y - 12, 0xFF88CCFF);
        y += 2 * ROW_HEIGHT + 4 + 2 * ROW_HEIGHT + 6;
        context.text(this.font, "— Private Messages —", centerX - this.font.width("— Private Messages —") / 2, y - 12, 0xFF88CCFF);
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}

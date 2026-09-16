package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatAppearanceConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int SLIDER_WIDTH = 200;
    private static final int FIELD_WIDTH = 50;
    private static final int FIELD_GAP = 6;
    private static final int LABEL_WIDTH = 170;
    private static final int HEX_FIELD_WIDTH = 90;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;

    public ChatAppearanceConfigScreen(Screen parent) {
        super(Component.literal("Tab Colors / Transparency"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        hexLabels.clear();

        int centerX = this.width / 2;
        int y = 40;

        y = addSliderRow(centerX, y, "Tab bar transparency", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabBarAlpha(), true, "%.0f",
                v -> config.setTabBarAlpha((int) v));

        y = addSliderRow(centerX, y, "Tab transparency", TibiaChatConfig.MIN_ALPHA, TibiaChatConfig.MAX_ALPHA,
                config.tabAlpha(), true, "%.0f",
                v -> config.setTabAlpha((int) v));

        y += 6;

        y = addHexColorRow(centerX, y, "Tab bar color (hex RRGGBB)", config.tabBarColor(),
                v -> config.setTabBarColor(v));

        y = addHexColorRow(centerX, y, "Tab color (hex RRGGBB)", config.tabColor(),
                v -> config.setTabColor(v));

        y += 12;

        this.addRenderableWidget(Button.builder(Component.literal("Reset Colors to Defaults"), btn -> {
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
                              boolean wholeNumber, String format, java.util.function.DoubleConsumer apply) {
        int sliderX = centerX - (SLIDER_WIDTH + FIELD_GAP + FIELD_WIDTH) / 2;
        int fieldX = sliderX + SLIDER_WIDTH + FIELD_GAP;

        ValueSlider slider = new ValueSlider(sliderX, y, SLIDER_WIDTH, 20, min, max, initial, wholeNumber,
                v -> Component.literal(label + ": " + String.format(Locale.ROOT, format, v)),
                v -> {
                    apply.accept(v);
                    config.save();
                });

        EditBox field = new EditBox(this.font, fieldX, y, FIELD_WIDTH, 20, Component.literal(label));
        field.setValue(String.format(Locale.ROOT, wholeNumber ? "%.0f" : "%.2f", initial));
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
        return y + ROW_HEIGHT;
    }

    private int addHexColorRow(int centerX, int y, String label, int initialRgb, IntConsumer apply) {
        int totalWidth = LABEL_WIDTH + FIELD_GAP + HEX_FIELD_WIDTH;
        int labelX = centerX - totalWidth / 2;
        int fieldX = labelX + LABEL_WIDTH + FIELD_GAP;

        EditBox field = new EditBox(this.font, fieldX, y, HEX_FIELD_WIDTH, 20, Component.literal(label));
        field.setMaxLength(7);
        field.setValue(String.format(Locale.ROOT, "%06X", initialRgb));
        field.setResponder(text -> {
            String hex = text.trim();
            if (hex.startsWith("#")) hex = hex.substring(1);
            if (!hex.matches("[0-9a-fA-F]{6}")) return;
            try {
                int parsed = Integer.parseInt(hex, 16);
                apply.accept(parsed);
                config.save();
            } catch (NumberFormatException ignored) {}
        });

        this.hexLabels.add(new HexLabel(label, labelX, y));
        this.addRenderableWidget(field);
        return y + ROW_HEIGHT;
    }

    private final java.util.List<HexLabel> hexLabels = new java.util.ArrayList<>();

    private record HexLabel(String text, int x, int y) {}

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        for (HexLabel h : hexLabels) {
            context.drawString(this.font, h.text(), h.x(), h.y() + 6, 0xFFFFFFFF, true);
        }
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}

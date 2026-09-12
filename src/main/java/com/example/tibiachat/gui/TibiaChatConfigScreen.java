package com.example.tibiachat.gui;

import com.example.tibiachat.TibiaChatTabsClient;
import com.example.tibiachat.config.TibiaChatConfig;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TibiaChatConfigScreen extends Screen {

    private static final int ROW_HEIGHT = 24;
    private static final int SLIDER_WIDTH = 200;
    private static final int FIELD_WIDTH = 50;
    private static final int FIELD_GAP = 6;

    private final Screen parent;
    private final TibiaChatConfig config = TibiaChatTabsClient.CONFIG;

    public TibiaChatConfigScreen(Screen parent) {
        super(Component.literal("Chat Tabs Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        y = addSliderRow(centerX, y, "Tab bar size", TibiaChatConfig.MIN_BAR_SCALE, TibiaChatConfig.MAX_BAR_SCALE,
                config.tabBarScale(), false, "%.2fx",
                v -> config.setTabBarScale((float) v));

        y = addSliderRow(centerX, y, "Notification icon size", TibiaChatConfig.MIN_ICON_SCALE, TibiaChatConfig.MAX_ICON_SCALE,
                config.notifIconScale(), false, "%.2fx",
                v -> config.setNotifIconScale((float) v));

        y += 8;

        this.addRenderableWidget(Button.builder(Component.literal("Reposition / Resize HUD"),
                        btn -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new HudEditScreen(this));
                            }
                        })
                .bounds(centerX - SLIDER_WIDTH / 2 - FIELD_GAP - FIELD_WIDTH / 2, y, SLIDER_WIDTH + FIELD_GAP + FIELD_WIDTH, 20)
                .build());
        y += ROW_HEIGHT + 12;

        this.addRenderableWidget(Button.builder(Component.literal("Reset to Defaults"), btn -> {
                    config.resetHudDefaults();
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
            } catch (NumberFormatException ignored) {
            }
        });

        this.addRenderableWidget(slider);
        this.addRenderableWidget(field);
        return y + ROW_HEIGHT;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }
}

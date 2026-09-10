package com.example.tibiachat.gui;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * A vanilla slider paired with an optional manual-entry text field elsewhere
 * on the screen. Dragging the slider calls {@link #onChange}; the owning
 * screen can also push a value in from a text field via
 * {@link #setRealValueSilently(double)} without re-triggering the callback.
 */
public class ValueSlider extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final boolean wholeNumber;
    private final DoubleFunction<Component> messageFactory;
    private final DoubleConsumer onChange;

    public ValueSlider(int x, int y, int width, int height, double min, double max,
                        double initialValue, boolean wholeNumber,
                        DoubleFunction<Component> messageFactory, DoubleConsumer onChange) {
        super(x, y, width, height, Component.empty(), normalize(initialValue, min, max));
        this.min = min;
        this.max = max;
        this.wholeNumber = wholeNumber;
        this.messageFactory = messageFactory;
        this.onChange = onChange;
        updateMessage();
    }

    private static double normalize(double v, double min, double max) {
        if (max <= min) return 0.0;
        return Math.max(0.0, Math.min(1.0, (v - min) / (max - min)));
    }

    public double getRealValue() {
        double v = min + (max - min) * this.value;
        return wholeNumber ? Math.round(v) : v;
    }

    /** Pushes a value in from an external source (e.g. the manual text field) without re-firing onChange. */
    public void setRealValueSilently(double v) {
        this.value = normalize(v, min, max);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(messageFactory.apply(getRealValue()));
    }

    @Override
    protected void applyValue() {
        onChange.accept(getRealValue());
    }
}

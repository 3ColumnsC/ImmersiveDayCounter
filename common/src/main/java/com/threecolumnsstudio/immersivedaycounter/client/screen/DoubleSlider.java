package com.threecolumnsstudio.immersivedaycounter.client.screen;

import java.util.Locale;
import java.util.function.DoubleConsumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public final class DoubleSlider extends AbstractSliderButton {

    private final Component label;
    private final double min;
    private final double max;
    private final double step;
    private final DoubleConsumer onValueChanged;

    public DoubleSlider(int x, int y, int width, int height, Component label, double min, double max, double step,
                        double initialValue, DoubleConsumer onValueChanged) {
        super(x, y, width, height, Component.empty(), toSliderValue(initialValue, min, max, step));
        this.label = label;
        this.min = min;
        this.max = max;
        this.step = step;
        this.onValueChanged = onValueChanged;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        if (this.label == null) {
            return;
        }
        setMessage(Component.translatable("idc.config.slider", this.label,
                String.format(Locale.ROOT, "%.1f", currentValue())));
    }

    @Override
    protected void applyValue() {
        updateMessage();
        if (this.onValueChanged != null) {
            this.onValueChanged.accept(currentValue());
        }
    }

    private double currentValue() {
        double raw = this.min + this.value * (this.max - this.min);
        double stepped = Math.round(raw / this.step) * this.step;
        return Math.max(this.min, Math.min(this.max, stepped));
    }

    private static double toSliderValue(double value, double min, double max, double step) {
        double clamped = Math.max(min, Math.min(max, value));
        double stepped = Math.round(clamped / step) * step;
        return (stepped - min) / (max - min);
    }
}

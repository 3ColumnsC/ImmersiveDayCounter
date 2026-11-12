package com.threecolumnsstudio.immersivedaycounter.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class NumberField extends EditBox {

    public static final int WIDTH = 100;
    private static final int HEIGHT = 20;
    private static final int MAX_LENGTH = 7;

    public NumberField(Font font, Component narration, long initialValue) {
        super(font, WIDTH, HEIGHT, narration);
        setMaxLength(MAX_LENGTH);
        setValue(Long.toString(initialValue));
    }

    public long value(long fallback) {
        String text = getValue().trim();
        if (text.isEmpty()) {
            return fallback;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

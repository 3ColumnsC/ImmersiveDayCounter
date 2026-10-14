package com.threecolumnsstudio.immersivedaycounter.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class ColorField extends EditBox {

    public static final int WIDTH = 100;
    private static final int HEIGHT = 20;
    private static final int MAX_LENGTH = 7;

    public ColorField(Font font, Component narration, String initialValue) {
        super(font, WIDTH, HEIGHT, narration);
        setMaxLength(MAX_LENGTH);
        setValue(initialValue == null ? "" : initialValue);
    }
}

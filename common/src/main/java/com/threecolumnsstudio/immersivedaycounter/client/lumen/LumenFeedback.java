package com.threecolumnsstudio.immersivedaycounter.client.lumen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public final class LumenFeedback {

    private static final int MESSAGE_COLOR = 0xFFFFFF;

    private LumenFeedback() {
    }

    public static void show(Minecraft minecraft, boolean enabled) {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        minecraft.gui.hud.setOverlayMessage(
            Component.translatable(enabled ? "idc.message.lumen.enabled" : "idc.message.lumen.disabled")
                .withColor(MESSAGE_COLOR),
            false
        );
    }
}

package com.threecolumnsstudio.immersivedaycounter.client;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class IDCSounds {

    private static final float DAILY_PITCH = 1.0F;

    private IDCSounds() {
    }

    public static void playDaily() {
        if (!IDCConfig.get().sounds.enabled) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, DAILY_PITCH));
    }
}

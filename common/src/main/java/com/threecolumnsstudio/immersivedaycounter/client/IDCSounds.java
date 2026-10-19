package com.threecolumnsstudio.immersivedaycounter.client;

import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class IDCSounds {

    private static final float DAILY_PITCH = 1.0F;

    private IDCSounds() {
    }

    public static void playDaily() {
        if (!IDCConfig.get().sounds.enabled) {
            return;
        }
        play(IDCConfig.get().sounds.sound);
    }

    public static void play(IDCConfig.DaySound sound) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(eventFor(sound), DAILY_PITCH));
    }

    private static SoundEvent eventFor(IDCConfig.DaySound sound) {
        return switch (sound) {
            case EXPERIENCE -> SoundEvents.EXPERIENCE_ORB_PICKUP;
            case LEVEL_UP -> SoundEvents.PLAYER_LEVELUP;
            case BELL -> SoundEvents.NOTE_BLOCK_BELL.value();
            case PICKUP -> SoundEvents.ITEM_PICKUP;
            case PLING -> SoundEvents.NOTE_BLOCK_PLING.value();
            case CREEPER -> SoundEvents.CREEPER_PRIMED;
            case CLICK -> SoundEvents.UI_BUTTON_CLICK.value();
        };
    }
}

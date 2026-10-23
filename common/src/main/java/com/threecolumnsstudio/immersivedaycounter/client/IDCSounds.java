package com.threecolumnsstudio.immersivedaycounter.client;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.config.IDCConfig;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class IDCSounds {

    private static final float DAILY_PITCH = 1.0F;
    private static final List<IDCConfig.DaySound> RANDOM_POOL = Arrays.stream(IDCConfig.DaySound.values())
        .filter(sound -> sound != IDCConfig.DaySound.RANDOM)
        .toList();

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
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(eventFor(resolve(sound)), DAILY_PITCH));
    }

    private static IDCConfig.DaySound resolve(IDCConfig.DaySound sound) {
        if (sound != IDCConfig.DaySound.RANDOM) {
            return sound;
        }
        return RANDOM_POOL.get(ThreadLocalRandom.current().nextInt(RANDOM_POOL.size()));
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
            case RANDOM -> unresolvedRandom();
        };
    }

    private static SoundEvent unresolvedRandom() {
        ImmersiveDayCounter.LOGGER.error("RANDOM reached IDCSounds.eventFor without being resolved; falling back to pling");
        return SoundEvents.NOTE_BLOCK_PLING.value();
    }
}

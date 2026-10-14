package com.threecolumnsstudio.immersivedaycounter.day;

import net.minecraft.world.level.Level;
import net.minecraft.world.timeline.Timelines;

public final class DayClock {

    public static final long TICKS_PER_DAY = 24000L;

    private DayClock() {
    }

    public static long currentDay(Level level) {
        return completedDays(level) + 1L;
    }

    public static long completedDays(Level level) {
        return level.registryAccess()
                .get(Timelines.OVERWORLD_DAY)
                .map(timeline -> (long) timeline.value().getPeriodCount(level.clockManager()))
                .orElseGet(() -> level.getOverworldClockTime() / TICKS_PER_DAY);
    }
}

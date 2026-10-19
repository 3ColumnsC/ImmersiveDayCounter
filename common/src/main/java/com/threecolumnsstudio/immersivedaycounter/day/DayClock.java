package com.threecolumnsstudio.immersivedaycounter.day;

import net.minecraft.world.level.Level;

public final class DayClock {

    public static final long TICKS_PER_DAY = 24000L;

    private DayClock() {
    }

    public static long currentDay(Level level) {
        return completedDays(level) + 1L;
    }

    public static long completedDays(Level level) {
        return level.getDayTime() / TICKS_PER_DAY;
    }

    public static long currentDayTicks(Level level) {
        return Math.floorMod(level.getDayTime(), TICKS_PER_DAY);
    }
}

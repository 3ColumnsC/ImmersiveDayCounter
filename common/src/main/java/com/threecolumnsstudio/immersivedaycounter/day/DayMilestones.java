package com.threecolumnsstudio.immersivedaycounter.day;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import java.util.List;
import net.minecraft.resources.Identifier;

public final class DayMilestones {

    public static final long FIRST = 1L;
    public static final long CENTURY = 100L;
    public static final long MILLENNIUM = 1000L;
    public static final long TEN_THOUSAND = 10000L;

    public static final List<Long> DAYS = List.of(FIRST, CENTURY, MILLENNIUM, TEN_THOUSAND);

    private DayMilestones() {
    }

    public static Identifier advancementId(long day) {
        return ImmersiveDayCounter.id("day_" + day);
    }
}

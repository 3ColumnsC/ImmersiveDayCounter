package com.threecolumnsstudio.immersivedaycounter.server;

import com.mojang.serialization.Codec;
import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class DayData extends SavedData {

    public static final long NONE = -1L;

    public static final Codec<DayData> CODEC = Codec.LONG.xmap(DayData::new, DayData::lastAnnouncedDay);

    public static final SavedDataType<DayData> TYPE =
            new SavedDataType<DayData>(ImmersiveDayCounter.id("day_data"), DayData::new, CODEC, null);

    private long lastAnnouncedDay;

    public DayData() {
        this(NONE);
    }

    private DayData(long lastAnnouncedDay) {
        this.lastAnnouncedDay = lastAnnouncedDay;
    }

    public long lastAnnouncedDay() {
        return this.lastAnnouncedDay;
    }

    public void setLastAnnouncedDay(long day) {
        this.lastAnnouncedDay = day;
        this.setDirty();
    }
}

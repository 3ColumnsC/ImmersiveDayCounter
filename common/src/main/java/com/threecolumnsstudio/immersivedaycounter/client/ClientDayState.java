package com.threecolumnsstudio.immersivedaycounter.client;

public final class ClientDayState {

    private static boolean known;
    private static long day;

    private ClientDayState() {
    }

    public static void update(long day, boolean announce) {
        ClientDayState.day = day;
        known = true;
        if (announce) {
            DisplayRenderer.announce(day);
        }
    }

    public static void reset() {
        known = false;
        day = 0L;
        DisplayRenderer.reset();
    }

    public static boolean isKnown() {
        return known;
    }

    public static long day() {
        return day;
    }
}

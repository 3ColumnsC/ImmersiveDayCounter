package com.threecolumnsstudio.immersivedaycounter.server;

import com.threecolumnsstudio.immersivedaycounter.day.DayClock;
import com.threecolumnsstudio.immersivedaycounter.day.DayMilestones;
import com.threecolumnsstudio.immersivedaycounter.network.DayPayload;
import com.threecolumnsstudio.immersivedaycounter.network.IDCNetwork;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class DayTracker {

    private static final Set<UUID> pendingGrants = new HashSet<>();

    private static long lastSeenDay;

    private DayTracker() {
    }

    public static void onServerStarted(MinecraftServer server) {
        pendingGrants.clear();
        lastSeenDay = DayClock.currentDay(server.overworld());
    }

    public static void onServerTick(MinecraftServer server) {
        grantPendingMilestones(server);

        long day = DayClock.currentDay(server.overworld());
        if (day == lastSeenDay) {
            return;
        }
        lastSeenDay = day;

        data(server.overworld()).setLastAnnouncedDay(day);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            IDCNetwork.sendToPlayer(player, new DayPayload(day, true));
            MilestoneGranter.grantUpTo(player, day);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }

        long day = DayClock.currentDay(server.overworld());
        DayData data = data(server.overworld());

        boolean announce = false;
        if (data.lastAnnouncedDay() == DayData.NONE) {
            announce = day <= DayMilestones.FIRST;
            data.setLastAnnouncedDay(day);
        }

        IDCNetwork.sendToPlayer(player, new DayPayload(day, announce));
        pendingGrants.add(player.getUUID());
    }

    private static void grantPendingMilestones(MinecraftServer server) {
        if (pendingGrants.isEmpty()) {
            return;
        }
        Iterator<UUID> iterator = pendingGrants.iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = server.getPlayerList().getPlayer(iterator.next());
            iterator.remove();
            if (player != null) {
                MilestoneGranter.grantTab(player);
                MilestoneGranter.grantUpTo(player, DayClock.currentDay(server.overworld()));
            }
        }
    }

    private static DayData data(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(DayData.TYPE);
    }
}

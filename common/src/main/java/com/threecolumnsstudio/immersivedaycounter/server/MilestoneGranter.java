package com.threecolumnsstudio.immersivedaycounter.server;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.day.DayMilestones;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public final class MilestoneGranter {

    private static final String CRITERION = "day";

    private MilestoneGranter() {
    }

    public static void grantTab(ServerPlayer player) {
        award(player, ImmersiveDayCounter.id("root"));
    }

    public static void grantUpTo(ServerPlayer player, long day) {
        for (long milestone : DayMilestones.DAYS) {
            if (day >= milestone) {
                award(player, DayMilestones.advancementId(milestone));
            }
        }
    }

    private static void award(ServerPlayer player, Identifier id) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        AdvancementHolder holder = server.getAdvancements().get(id);
        if (holder == null) {
            return;
        }
        PlayerAdvancements advancements = player.getAdvancements();
        advancements.award(holder, CRITERION);
    }
}

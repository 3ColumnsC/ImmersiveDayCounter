package com.threecolumnsstudio.immersivedaycounter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.threecolumnsstudio.immersivedaycounter.day.DayClock;
import com.threecolumnsstudio.immersivedaycounter.day.DayMilestones;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class IDCCommand {

    public static final long MIN_DAY = DayMilestones.FIRST;
    public static final long MAX_DAY = 1_000_000L;

    private IDCCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("idc")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(
                                Commands.literal("set")
                                        .then(
                                                Commands.literal("day")
                                                        .then(
                                                                Commands.argument("day", LongArgumentType.longArg(MIN_DAY, MAX_DAY))
                                                                        .executes(IDCCommand::setDay)))));
    }

    private static int setDay(CommandContext<CommandSourceStack> context) {
        long day = LongArgumentType.getLong(context, "day");
        CommandSourceStack source = context.getSource();
        source.getServer().overworld().setDayTime((day - 1L) * DayClock.TICKS_PER_DAY);
        source.sendSuccess(() -> Component.translatable("commands.idc.set.day", day), true);
        return (int) Math.min(day, Integer.MAX_VALUE);
    }
}

package com.threecolumnsstudio.immersivedaycounter.network;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DayPayload(long day, boolean announce) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DayPayload> TYPE =
            new CustomPacketPayload.Type<>(ImmersiveDayCounter.id("day"));

    public static final StreamCodec<FriendlyByteBuf, DayPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, DayPayload::day,
            ByteBufCodecs.BOOL, DayPayload::announce,
            DayPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

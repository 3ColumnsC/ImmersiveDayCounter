package com.threecolumnsstudio.immersivedaycounter.neoforge;

import com.threecolumnsstudio.immersivedaycounter.ImmersiveDayCounter;
import com.threecolumnsstudio.immersivedaycounter.client.ClientDayState;
import com.threecolumnsstudio.immersivedaycounter.command.IDCCommand;
import com.threecolumnsstudio.immersivedaycounter.network.DayPayload;
import com.threecolumnsstudio.immersivedaycounter.network.IDCNetwork;
import com.threecolumnsstudio.immersivedaycounter.server.DayTracker;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(ImmersiveDayCounter.MOD_ID)
public final class IDCNeoForge {

    public IDCNeoForge(IEventBus modEventBus) {
        IDCNetwork.setSender((player, payload) -> PacketDistributor.sendToPlayer(player, payload));

        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(DayPayload.TYPE, DayPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientDayState.update(payload.day(), payload.announce())));
    }

    private void registerCommands(RegisterCommandsEvent event) {
        IDCCommand.register(event.getDispatcher());
    }

    private void onServerStarted(ServerStartedEvent event) {
        DayTracker.onServerStarted(event.getServer());
    }

    private void onServerTick(ServerTickEvent.Post event) {
        DayTracker.onServerTick(event.getServer());
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DayTracker.onPlayerJoin(player);
        }
    }
}

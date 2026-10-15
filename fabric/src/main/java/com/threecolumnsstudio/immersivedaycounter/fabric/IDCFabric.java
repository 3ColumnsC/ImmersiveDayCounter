package com.threecolumnsstudio.immersivedaycounter.fabric;

import com.threecolumnsstudio.immersivedaycounter.command.IDCCommand;
import com.threecolumnsstudio.immersivedaycounter.network.DayPayload;
import com.threecolumnsstudio.immersivedaycounter.network.IDCNetwork;
import com.threecolumnsstudio.immersivedaycounter.server.DayTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class IDCFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(DayPayload.TYPE, DayPayload.STREAM_CODEC);
        IDCNetwork.setSender(ServerPlayNetworking::send);

        ServerLifecycleEvents.SERVER_STARTED.register(DayTracker::onServerStarted);
        ServerTickEvents.END_SERVER_TICK.register(DayTracker::onServerTick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> DayTracker.onPlayerJoin(handler.getPlayer()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> IDCCommand.register(dispatcher));
    }
}
